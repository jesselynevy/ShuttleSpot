-- Driver identity comes from the JWT so callers cannot operate another driver's trip.

create unique index trips_one_open_assignment_per_driver_idx
  on public.trips (driver_id)
  where status in (
    'not_started'::public.trip_status,
    'active'::public.trip_status
  );

create or replace function public.driver_trip_snapshot(
  p_trip_id uuid,
  p_driver_id uuid
)
returns jsonb
language sql
stable
security definer
set search_path = ''
as $$
  select jsonb_build_object(
    'trip_id', trips.id,
    'route_id', trips.route_id,
    'vehicle_label', vehicles.label,
    'status', trips.status::text,
    'current_stop_name', current_stop.name,
    'next_stop_name', next_stop.name,
    'current_stop_sequence', trips.current_stop_sequence,
    'total_stops', coalesce(stop_summary.total_stops, 0),
    'waiting_passenger_count', coalesce(waiting.waiting_count, 0)
  )
  from public.trips
  join public.vehicles on vehicles.id = trips.vehicle_id
  left join lateral (
    select max(sequence)::integer as total_stops
    from public.stops
    where stops.route_id = trips.route_id
  ) stop_summary on true
  left join public.stops current_stop
    on current_stop.route_id = trips.route_id
   and current_stop.sequence = trips.current_stop_sequence
  left join public.stops next_stop
    on next_stop.route_id = trips.route_id
   and next_stop.sequence = trips.current_stop_sequence + 1
  left join lateral (
    select count(*)::integer as waiting_count
    from public.boarding_intents
    join public.stops intent_stop
      on intent_stop.id = boarding_intents.stop_id
    where boarding_intents.trip_id = trips.id
      and boarding_intents.status = 'confirmed'::public.boarding_intent_status
      and intent_stop.sequence = trips.current_stop_sequence + 1
  ) waiting on true
  where trips.id = p_trip_id
    and trips.driver_id = p_driver_id;
$$;

create or replace function public.get_assigned_trip()
returns jsonb
language plpgsql
stable
security definer
set search_path = ''
as $$
declare
  caller_id uuid := auth.uid();
  assigned_trip_id uuid;
begin
  if caller_id is null or not public.current_driver_is_ready() then
    raise exception 'Active driver with changed password required'
      using errcode = '42501';
  end if;

  select id into assigned_trip_id
  from public.trips
  where driver_id = caller_id
    and status in (
      'not_started'::public.trip_status,
      'active'::public.trip_status
    )
  order by case status
    when 'active'::public.trip_status then 0
    else 1
  end, updated_at desc
  limit 1;

  if assigned_trip_id is null then
    return null;
  end if;

  return public.driver_trip_snapshot(assigned_trip_id, caller_id);
end;
$$;

create or replace function public.start_assigned_trip(p_trip_id uuid)
returns jsonb
language plpgsql
security definer
set search_path = ''
as $$
declare
  caller_id uuid := auth.uid();
  trip_record public.trips;
begin
  if caller_id is null or not public.current_driver_is_ready() then
    raise exception 'Active driver with changed password required'
      using errcode = '42501';
  end if;

  select * into trip_record
  from public.trips
  where id = p_trip_id and driver_id = caller_id
  for update;

  if not found then
    raise exception 'Assigned trip not found' using errcode = '42501';
  end if;

  if trip_record.status <> 'not_started'::public.trip_status then
    raise exception 'Trip can only start once' using errcode = '23514';
  end if;

  update public.trips
  set status = 'active'::public.trip_status,
      started_at = now(),
      ended_at = null
  where id = p_trip_id;

  return public.driver_trip_snapshot(p_trip_id, caller_id);
end;
$$;

create or replace function public.advance_trip_stop(p_trip_id uuid)
returns jsonb
language plpgsql
security definer
set search_path = ''
as $$
declare
  caller_id uuid := auth.uid();
  trip_record public.trips;
  last_stop_sequence integer;
begin
  if caller_id is null or not public.current_driver_is_ready() then
    raise exception 'Active driver with changed password required'
      using errcode = '42501';
  end if;

  select * into trip_record
  from public.trips
  where id = p_trip_id and driver_id = caller_id
  for update;

  if not found then
    raise exception 'Assigned trip not found' using errcode = '42501';
  end if;

  if trip_record.status <> 'active'::public.trip_status then
    raise exception 'Only an active trip can advance' using errcode = '23514';
  end if;

  select max(sequence) into last_stop_sequence
  from public.stops
  where route_id = trip_record.route_id;

  if last_stop_sequence is null
     or trip_record.current_stop_sequence >= last_stop_sequence then
    raise exception 'Trip is already at the final stop' using errcode = '23514';
  end if;

  update public.trips
  set current_stop_sequence = current_stop_sequence + 1
  where id = p_trip_id;

  return public.driver_trip_snapshot(p_trip_id, caller_id);
end;
$$;

create or replace function public.finish_assigned_trip(p_trip_id uuid)
returns jsonb
language plpgsql
security definer
set search_path = ''
as $$
declare
  caller_id uuid := auth.uid();
  trip_record public.trips;
  last_stop_sequence integer;
begin
  if caller_id is null or not public.current_driver_is_ready() then
    raise exception 'Active driver with changed password required'
      using errcode = '42501';
  end if;

  select * into trip_record
  from public.trips
  where id = p_trip_id and driver_id = caller_id
  for update;

  if not found then
    raise exception 'Assigned trip not found' using errcode = '42501';
  end if;

  if trip_record.status <> 'active'::public.trip_status then
    raise exception 'Only an active trip can finish' using errcode = '23514';
  end if;

  select max(sequence) into last_stop_sequence
  from public.stops
  where route_id = trip_record.route_id;

  if last_stop_sequence is null
     or trip_record.current_stop_sequence < last_stop_sequence then
    raise exception 'Reach the final stop before finishing the trip'
      using errcode = '23514';
  end if;

  update public.trips
  set status = 'completed'::public.trip_status,
      ended_at = now()
  where id = p_trip_id;

  return public.driver_trip_snapshot(p_trip_id, caller_id);
end;
$$;

create or replace function public.get_stop_demand(p_trip_id uuid)
returns table (
  stop_id uuid,
  stop_sequence integer,
  stop_name text,
  waiting_passenger_count bigint
)
language plpgsql
stable
security definer
set search_path = ''
as $$
declare
  caller_id uuid := auth.uid();
begin
  if caller_id is null or not public.current_driver_is_ready() then
    raise exception 'Active driver with changed password required'
      using errcode = '42501';
  end if;

  if not exists (
    select 1 from public.trips
    where id = p_trip_id and driver_id = caller_id
  ) then
    raise exception 'Assigned trip not found' using errcode = '42501';
  end if;

  return query
  select stops.id,
         stops.sequence,
         stops.name,
         count(boarding_intents.id) filter (
           where boarding_intents.status = 'confirmed'::public.boarding_intent_status
         )
  from public.trips
  join public.stops on stops.route_id = trips.route_id
  left join public.boarding_intents
    on boarding_intents.trip_id = trips.id
   and boarding_intents.stop_id = stops.id
  where trips.id = p_trip_id
    and trips.driver_id = caller_id
  group by stops.id, stops.sequence, stops.name
  order by stops.sequence;
end;
$$;

revoke all on function public.driver_trip_snapshot(uuid, uuid)
  from public, anon, authenticated;
revoke all on function public.get_assigned_trip()
  from public, anon, authenticated;
revoke all on function public.start_assigned_trip(uuid)
  from public, anon, authenticated;
revoke all on function public.advance_trip_stop(uuid)
  from public, anon, authenticated;
revoke all on function public.finish_assigned_trip(uuid)
  from public, anon, authenticated;
revoke all on function public.get_stop_demand(uuid)
  from public, anon, authenticated;

grant execute on function public.get_assigned_trip() to authenticated;
grant execute on function public.start_assigned_trip(uuid) to authenticated;
grant execute on function public.advance_trip_stop(uuid) to authenticated;
grant execute on function public.finish_assigned_trip(uuid) to authenticated;
grant execute on function public.get_stop_demand(uuid) to authenticated;

drop policy if exists "driver updates assigned trip" on public.trips;
revoke update on table public.trips from anon, authenticated;
grant select on table public.trips to authenticated;
