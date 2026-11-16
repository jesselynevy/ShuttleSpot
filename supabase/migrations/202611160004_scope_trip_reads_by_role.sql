-- Passengers may read active service; drivers may read only trips assigned to
-- them. This prevents a driver account from browsing another driver's trip.

drop policy if exists "read active trips" on public.trips;

create policy "passengers read active trips"
on public.trips
for select
to authenticated
using (
  status = 'active'::public.trip_status
  and exists (
    select 1 from public.profiles
    where profiles.id = auth.uid()
      and profiles.role = 'passenger'::public.user_role
  )
);

create policy "drivers read assigned trips"
on public.trips
for select
to authenticated
using (
  driver_id = auth.uid()
  and exists (
    select 1 from public.profiles
    where profiles.id = auth.uid()
      and profiles.role = 'driver'::public.user_role
  )
);
