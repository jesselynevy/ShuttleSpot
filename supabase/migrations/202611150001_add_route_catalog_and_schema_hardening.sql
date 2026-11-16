-- Adds the route data and integrity guarantees required by the offline-first
-- Android contract. This migration is additive to 202610050001_initial_shuttlespot.sql.

create type public.boarding_intent_status as enum ('confirmed', 'cancelled');

alter table public.routes
  add column service_notice text,
  add column created_at timestamptz not null default now(),
  add column updated_at timestamptz not null default now();

alter table public.profiles
  add column updated_at timestamptz not null default now();

alter table public.vehicles
  add column label text;

update public.vehicles
set label = plate_number
where label is null;

alter table public.vehicles
  alter column label set not null;

alter table public.trips
  add column updated_at timestamptz not null default now(),
  add constraint trips_current_stop_sequence_non_negative
    check (current_stop_sequence >= 0),
  add constraint trips_end_after_start
    check (ended_at is null or started_at is null or ended_at >= started_at);

alter table public.vehicle_locations
  add column created_at timestamptz not null default now();

alter table public.boarding_intents
  alter column status drop default,
  alter column status type public.boarding_intent_status
    using status::public.boarding_intent_status,
  alter column status set default 'confirmed'::public.boarding_intent_status,
  add column updated_at timestamptz not null default now();

alter table public.announcements
  add column created_at timestamptz not null default now();

alter table public.notification_subscriptions
  add column updated_at timestamptz not null default now();

create table public.route_geometry_points (
  route_id uuid not null references public.routes(id) on delete cascade,
  sequence integer not null check (sequence >= 0),
  latitude double precision not null,
  longitude double precision not null,
  primary key (route_id, sequence)
);

create table public.route_timetable_entries (
  route_id uuid not null references public.routes(id) on delete cascade,
  sequence integer not null check (sequence >= 0),
  period text not null,
  departure_time time not null,
  description text not null default '',
  primary key (route_id, sequence)
);

alter table public.stops
  add constraint stops_sequence_non_negative check (sequence >= 0);

create index boarding_intents_trip_stop_idx
  on public.boarding_intents (trip_id, stop_id);

create index notification_subscriptions_route_idx
  on public.notification_subscriptions (route_id)
  where route_id is not null;

create or replace function public.set_updated_at()
returns trigger
language plpgsql
set search_path = ''
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

revoke all on function public.set_updated_at() from public;

create or replace function public.validate_passenger_preference_stops()
returns trigger
language plpgsql
set search_path = ''
as $$
declare
  pickup_route_id uuid;
  destination_route_id uuid;
  pickup_sequence integer;
  destination_sequence integer;
begin
  select route_id, sequence
    into pickup_route_id, pickup_sequence
    from public.stops
   where id = new.pickup_stop_id;

  select route_id, sequence
    into destination_route_id, destination_sequence
    from public.stops
   where id = new.destination_stop_id;

  if pickup_route_id is null or destination_route_id is null then
    raise exception 'Pickup and destination stops must exist'
      using errcode = '23503';
  end if;

  if pickup_route_id <> new.route_id or destination_route_id <> new.route_id then
    raise exception 'Pickup and destination stops must belong to the selected route'
      using errcode = '23514';
  end if;

  if destination_sequence <= pickup_sequence then
    raise exception 'Destination stop must be after pickup stop'
      using errcode = '23514';
  end if;

  return new;
end;
$$;

revoke all on function public.validate_passenger_preference_stops() from public;

create or replace function public.delete_expired_vehicle_locations()
returns integer
language plpgsql
set search_path = ''
as $$
declare
  deleted_count integer;
begin
  delete from public.vehicle_locations
   where captured_at < now() - interval '24 hours';

  get diagnostics deleted_count = row_count;
  return deleted_count;
end;
$$;

revoke all on function public.delete_expired_vehicle_locations() from public;

drop trigger if exists profiles_set_updated_at on public.profiles;
create trigger profiles_set_updated_at
before update on public.profiles
for each row execute function public.set_updated_at();

drop trigger if exists routes_set_updated_at on public.routes;
create trigger routes_set_updated_at
before update on public.routes
for each row execute function public.set_updated_at();

drop trigger if exists passenger_preferences_set_updated_at on public.passenger_preferences;
create trigger passenger_preferences_set_updated_at
before update on public.passenger_preferences
for each row execute function public.set_updated_at();

drop trigger if exists trips_set_updated_at on public.trips;
create trigger trips_set_updated_at
before update on public.trips
for each row execute function public.set_updated_at();

drop trigger if exists boarding_intents_set_updated_at on public.boarding_intents;
create trigger boarding_intents_set_updated_at
before update on public.boarding_intents
for each row execute function public.set_updated_at();

drop trigger if exists notification_subscriptions_set_updated_at on public.notification_subscriptions;
create trigger notification_subscriptions_set_updated_at
before update on public.notification_subscriptions
for each row execute function public.set_updated_at();

drop trigger if exists passenger_preferences_validate_stops on public.passenger_preferences;
create trigger passenger_preferences_validate_stops
before insert or update of route_id, pickup_stop_id, destination_stop_id
on public.passenger_preferences
for each row execute function public.validate_passenger_preference_stops();
