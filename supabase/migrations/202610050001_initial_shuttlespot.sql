create extension if not exists "pgcrypto";

create type public.user_role as enum ('passenger', 'driver', 'admin');
create type public.trip_status as enum ('not_started', 'active', 'completed', 'cancelled');

create table public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  role public.user_role not null,
  display_name text not null,
  email text unique,
  phone_e164 text unique,
  created_at timestamptz not null default now(),
  check ((role = 'passenger' and email is not null) or (role = 'driver' and phone_e164 is not null) or role = 'admin')
);
create table public.routes (id uuid primary key default gen_random_uuid(), name text not null, direction text not null, active boolean not null default true);
create table public.stops (id uuid primary key default gen_random_uuid(), route_id uuid not null references public.routes(id), name text not null, sequence integer not null, latitude double precision not null, longitude double precision not null, unique(route_id, sequence));
create table public.vehicles (id uuid primary key default gen_random_uuid(), plate_number text not null unique, capacity integer not null check(capacity > 0), active boolean not null default true);
create table public.trips (id uuid primary key default gen_random_uuid(), route_id uuid not null references public.routes(id), vehicle_id uuid not null references public.vehicles(id), driver_id uuid not null references public.profiles(id), status public.trip_status not null default 'not_started', current_stop_sequence integer not null default 0, started_at timestamptz, ended_at timestamptz);
create table public.vehicle_locations (id uuid primary key default gen_random_uuid(), trip_id uuid not null references public.trips(id) on delete cascade, latitude double precision not null, longitude double precision not null, accuracy_meters real, captured_at timestamptz not null);
create table public.passenger_preferences (profile_id uuid primary key references public.profiles(id) on delete cascade, route_id uuid not null references public.routes(id), pickup_stop_id uuid not null references public.stops(id), destination_stop_id uuid not null references public.stops(id), updated_at timestamptz not null default now());
create table public.boarding_intents (id uuid primary key default gen_random_uuid(), passenger_id uuid not null references public.profiles(id), trip_id uuid not null references public.trips(id), stop_id uuid not null references public.stops(id), status text not null default 'confirmed', idempotency_key uuid not null unique, created_at timestamptz not null default now(), unique(passenger_id, trip_id, stop_id));
create table public.announcements (id uuid primary key default gen_random_uuid(), author_id uuid not null references public.profiles(id), route_id uuid references public.routes(id), title text not null, body text not null, published_at timestamptz not null default now());
create table public.notification_subscriptions (profile_id uuid not null references public.profiles(id) on delete cascade, route_id uuid references public.routes(id), fcm_token text not null, preferences jsonb not null default '{}'::jsonb, primary key(profile_id, fcm_token));

create index vehicle_locations_trip_captured_at_idx on public.vehicle_locations(trip_id, captured_at desc);
create index trips_route_status_idx on public.trips(route_id, status);
create index announcements_route_published_at_idx on public.announcements(route_id, published_at desc);

alter table public.profiles enable row level security;
alter table public.passenger_preferences enable row level security;
alter table public.boarding_intents enable row level security;
alter table public.trips enable row level security;
alter table public.vehicle_locations enable row level security;
alter table public.routes enable row level security;
alter table public.stops enable row level security;
alter table public.announcements enable row level security;
alter table public.notification_subscriptions enable row level security;

-- Production authentication must map auth.uid() to profiles.id. Demo adapters do not execute this migration.
create policy "read active route data" on public.routes for select using (active);
create policy "read route stops" on public.stops for select using (true);
create policy "read published announcements" on public.announcements for select using (true);
create policy "read active trips" on public.trips for select using (status = 'active');
create policy "read active locations" on public.vehicle_locations for select using (exists (select 1 from public.trips where trips.id = vehicle_locations.trip_id and trips.status = 'active'));
create policy "manage own profile" on public.profiles for all using (id = auth.uid()) with check (id = auth.uid());
create policy "manage own preferences" on public.passenger_preferences for all using (profile_id = auth.uid()) with check (profile_id = auth.uid());
create policy "manage own boarding intent" on public.boarding_intents for all using (passenger_id = auth.uid()) with check (passenger_id = auth.uid());
create policy "driver updates assigned trip" on public.trips for update using (driver_id = auth.uid() and status in ('not_started', 'active')) with check (driver_id = auth.uid());
create policy "driver inserts active location" on public.vehicle_locations for insert with check (exists (select 1 from public.trips where trips.id = vehicle_locations.trip_id and trips.driver_id = auth.uid() and trips.status = 'active'));
create policy "driver authors announcement" on public.announcements for insert with check (author_id = auth.uid());
create policy "manage own subscriptions" on public.notification_subscriptions for all using (profile_id = auth.uid()) with check (profile_id = auth.uid());
