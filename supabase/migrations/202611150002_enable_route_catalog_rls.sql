-- Enables RLS for tables added after the initial migration and grants only the
-- read access required for the public active-route catalogue.

alter table public.vehicles enable row level security;
alter table public.route_geometry_points enable row level security;
alter table public.route_timetable_entries enable row level security;

drop policy if exists "read route stops" on public.stops;

create policy "read active route stops"
on public.stops
for select
to anon, authenticated
using (
  exists (
    select 1
    from public.routes
    where routes.id = stops.route_id
      and routes.active
  )
);

create policy "read active route geometry"
on public.route_geometry_points
for select
to anon, authenticated
using (
  exists (
    select 1
    from public.routes
    where routes.id = route_geometry_points.route_id
      and routes.active
  )
);

create policy "read active route timetables"
on public.route_timetable_entries
for select
to anon, authenticated
using (
  exists (
    select 1
    from public.routes
    where routes.id = route_timetable_entries.route_id
      and routes.active
  )
);

create policy "read active vehicles"
on public.vehicles
for select
to anon, authenticated
using (active);
