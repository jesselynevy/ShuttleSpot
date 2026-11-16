-- Development-only public route catalogue.
-- Stable UUIDs make future tests, trips, and foreign keys predictable.

insert into public.routes (
  id,
  name,
  direction,
  active,
  service_notice
)
values (
  '10000000-0000-0000-0000-000000000001',
  'Pasar Modern BSD ke UMN',
  'BSD → UMN',
  true,
  null
)
on conflict (id) do update
set
  name = excluded.name,
  direction = excluded.direction,
  active = excluded.active,
  service_notice = excluded.service_notice;

insert into public.stops (
  id,
  route_id,
  name,
  sequence,
  latitude,
  longitude
)
values
  ('11000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'Halte Pasar Modern BSD', 1, -6.3045281, 106.6848223),
  ('11000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', 'Halte Seberang Telkom', 2, -6.277908133873632, 106.66122527545618),
  ('11000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001', 'Halte depan Mall WTC Villa Melati Mas', 3, -6.271424137283743, 106.6554922228125),
  ('11000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000001', 'Halte Assobirin', 4, -6.258629712030424, 106.65182741364757),
  ('11000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000001', 'Halte Bundaran Alam Sutera', 5, -6.24412, 106.64886),
  ('11000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000001', 'Gerbang Gading Serpong', 6, -6.23077, 106.64136),
  ('11000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000001', 'Paramount', 7, -6.2307640413527325, 106.64040084798098),
  ('11000000-0000-0000-0000-000000000008', '10000000-0000-0000-0000-000000000001', 'Dormitory', 8, -6.251652290980524, 106.62814256878931),
  ('11000000-0000-0000-0000-000000000009', '10000000-0000-0000-0000-000000000001', 'UMN', 9, -6.2567379, 106.6183029)
on conflict (id) do update
set
  route_id = excluded.route_id,
  name = excluded.name,
  sequence = excluded.sequence,
  latitude = excluded.latitude,
  longitude = excluded.longitude;

-- Valid fallback geometry: ordered stop coordinates.
-- Later, replace these with the decoded OSRM road polyline already held in RoutePaths.
insert into public.route_geometry_points (
  route_id,
  sequence,
  latitude,
  longitude
)
values
  ('10000000-0000-0000-0000-000000000001', 1, -6.3045281, 106.6848223),
  ('10000000-0000-0000-0000-000000000001', 2, -6.277908133873632, 106.66122527545618),
  ('10000000-0000-0000-0000-000000000001', 3, -6.271424137283743, 106.6554922228125),
  ('10000000-0000-0000-0000-000000000001', 4, -6.258629712030424, 106.65182741364757),
  ('10000000-0000-0000-0000-000000000001', 5, -6.24412, 106.64886),
  ('10000000-0000-0000-0000-000000000001', 6, -6.23077, 106.64136),
  ('10000000-0000-0000-0000-000000000001', 7, -6.2307640413527325, 106.64040084798098),
  ('10000000-0000-0000-0000-000000000001', 8, -6.251652290980524, 106.62814256878931),
  ('10000000-0000-0000-0000-000000000001', 9, -6.2567379, 106.6183029)
on conflict (route_id, sequence) do update
set
  latitude = excluded.latitude,
  longitude = excluded.longitude;

insert into public.route_timetable_entries (
  route_id,
  sequence,
  period,
  departure_time,
  description
)
values
  ('10000000-0000-0000-0000-000000000001', 1, 'Pagi', '06.15', 'Halte Pasar Modern BSD – Halte Seberang Telkom – Halte depan Mall WTC Villa Melati Mas – Halte Assobirin – Halte Bundaran Alam Sutera'),
  ('10000000-0000-0000-0000-000000000001', 2, 'Pagi', '07.00', 'Gerbang Gading Serpong – Paramount'),
  ('10000000-0000-0000-0000-000000000001', 3, 'Pagi', '07.10', 'Dormitory – UMN'),
  ('10000000-0000-0000-0000-000000000001', 4, 'Pagi', '07.20', 'Dormitory'),
  ('10000000-0000-0000-0000-000000000001', 5, 'Pagi', '07.30', 'Tiba UMN')
on conflict (route_id, sequence) do update
set
  period = excluded.period,
  departure_time = excluded.departure_time,
  description = excluded.description;
