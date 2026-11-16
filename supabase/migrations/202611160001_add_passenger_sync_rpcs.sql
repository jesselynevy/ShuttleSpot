-- Authenticated, idempotent mutation boundary used by the Android outbox.
-- Client-provided profile IDs are intentionally not accepted: ownership always
-- comes from auth.uid().

create or replace function public.save_passenger_preference(
  p_route_id uuid,
  p_pickup_stop_id uuid,
  p_destination_stop_id uuid
)
returns public.passenger_preferences
language plpgsql
security definer
set search_path = ''
as $$
declare
  caller_id uuid := auth.uid();
  saved_preference public.passenger_preferences;
begin
  if caller_id is null or not exists (
    select 1 from public.profiles
    where id = caller_id and role = 'passenger'::public.user_role
  ) then
    raise exception 'Authenticated passenger required' using errcode = '42501';
  end if;

  insert into public.passenger_preferences (
    profile_id,
    route_id,
    pickup_stop_id,
    destination_stop_id
  ) values (
    caller_id,
    p_route_id,
    p_pickup_stop_id,
    p_destination_stop_id
  )
  on conflict (profile_id) do update set
    route_id = excluded.route_id,
    pickup_stop_id = excluded.pickup_stop_id,
    destination_stop_id = excluded.destination_stop_id
  returning * into saved_preference;

  return saved_preference;
end;
$$;

create or replace function public.submit_boarding_intent(
  p_trip_id uuid,
  p_stop_id uuid,
  p_idempotency_key uuid
)
returns public.boarding_intents
language plpgsql
security definer
set search_path = ''
as $$
declare
  caller_id uuid := auth.uid();
  trip_route_id uuid;
  saved_intent public.boarding_intents;
begin
  if caller_id is null or not exists (
    select 1 from public.profiles
    where id = caller_id and role = 'passenger'::public.user_role
  ) then
    raise exception 'Authenticated passenger required' using errcode = '42501';
  end if;

  select route_id into trip_route_id
  from public.trips
  where id = p_trip_id and status = 'active'::public.trip_status;

  if trip_route_id is null then
    raise exception 'Active trip not found' using errcode = '23503';
  end if;

  if not exists (
    select 1 from public.stops
    where id = p_stop_id and route_id = trip_route_id
  ) then
    raise exception 'Boarding stop must belong to the trip route'
      using errcode = '23514';
  end if;

  select * into saved_intent
  from public.boarding_intents
  where idempotency_key = p_idempotency_key;

  if found then
    if saved_intent.passenger_id <> caller_id
       or saved_intent.trip_id <> p_trip_id
       or saved_intent.stop_id <> p_stop_id then
      raise exception 'Idempotency key already belongs to another request'
        using errcode = '23505';
    end if;
    return saved_intent;
  end if;

  insert into public.boarding_intents (
    passenger_id,
    trip_id,
    stop_id,
    status,
    idempotency_key
  ) values (
    caller_id,
    p_trip_id,
    p_stop_id,
    'confirmed'::public.boarding_intent_status,
    p_idempotency_key
  )
  on conflict (passenger_id, trip_id, stop_id) do update set
    status = 'confirmed'::public.boarding_intent_status
  returning * into saved_intent;

  return saved_intent;
end;
$$;

create or replace function public.cancel_boarding_intent(
  p_trip_id uuid,
  p_stop_id uuid
)
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
  caller_id uuid := auth.uid();
begin
  if caller_id is null or not exists (
    select 1 from public.profiles
    where id = caller_id and role = 'passenger'::public.user_role
  ) then
    raise exception 'Authenticated passenger required' using errcode = '42501';
  end if;

  update public.boarding_intents
  set status = 'cancelled'::public.boarding_intent_status
  where passenger_id = caller_id
    and trip_id = p_trip_id
    and stop_id = p_stop_id;
end;
$$;

revoke all on function public.save_passenger_preference(uuid, uuid, uuid)
  from public, anon, authenticated;
revoke all on function public.submit_boarding_intent(uuid, uuid, uuid)
  from public, anon, authenticated;
revoke all on function public.cancel_boarding_intent(uuid, uuid)
  from public, anon, authenticated;

grant execute on function public.save_passenger_preference(uuid, uuid, uuid)
  to authenticated;
grant execute on function public.submit_boarding_intent(uuid, uuid, uuid)
  to authenticated;
grant execute on function public.cancel_boarding_intent(uuid, uuid)
  to authenticated;

-- Passenger mutations go through the validated RPCs above. Direct table reads
-- remain owner-scoped by RLS, while direct writes are denied.
revoke insert, update, delete on table public.passenger_preferences
  from anon, authenticated;
revoke insert, update, delete on table public.boarding_intents
  from anon, authenticated;
grant select on table public.passenger_preferences to authenticated;
grant select on table public.boarding_intents to authenticated;

drop policy if exists "driver updates assigned trip" on public.trips;
create policy "driver updates assigned trip"
on public.trips
for update
to authenticated
using (
  driver_id = auth.uid()
  and status in ('not_started'::public.trip_status, 'active'::public.trip_status)
  and exists (
    select 1 from public.profiles
    where profiles.id = auth.uid()
      and profiles.role = 'driver'::public.user_role
  )
)
with check (driver_id = auth.uid());

grant select, update on table public.trips to authenticated;
