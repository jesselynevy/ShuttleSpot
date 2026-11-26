create or replace function public.publish_vehicle_location(
  p_trip_id uuid,
  p_latitude double precision,
  p_longitude double precision,
  p_accuracy_meters real,
  p_captured_at timestamptz
)
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
  caller_id uuid := auth.uid();
begin
  if caller_id is null or not public.current_driver_is_ready() then
    raise exception 'Active driver required' using errcode = '42501';
  end if;

  if not exists (
    select 1 from public.trips
    where id = p_trip_id
      and driver_id = caller_id
      and status = 'active'::public.trip_status
  ) then
    raise exception 'Trip is not your active trip' using errcode = '42501';
  end if;

  if p_latitude is null or p_latitude < -90 or p_latitude > 90
     or p_longitude is null or p_longitude < -180 or p_longitude > 180 then
    raise exception 'Invalid coordinates' using errcode = '22023';
  end if;

  if p_accuracy_meters is not null and p_accuracy_meters < 0 then
    raise exception 'Invalid accuracy' using errcode = '22023';
  end if;

  if p_captured_at is null or p_captured_at > now() + interval '1 minute' then
    raise exception 'Invalid timestamp' using errcode = '22023';
  end if;

  insert into public.vehicle_locations (trip_id, latitude, longitude, accuracy_meters, captured_at)
  values (p_trip_id, p_latitude, p_longitude, p_accuracy_meters, p_captured_at);
end;
$$;

revoke all on function public.publish_vehicle_location(uuid, double precision, double precision, real, timestamptz)
  from public, anon, authenticated;
grant execute on function public.publish_vehicle_location(uuid, double precision, double precision, real, timestamptz) to authenticated;

-- Hardening: paksa semua insert lewat RPC, bukan insert langsung
drop policy if exists "driver inserts active location" on public.vehicle_locations;
revoke insert on table public.vehicle_locations from anon, authenticated;