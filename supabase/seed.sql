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

-- Remaining verified route catalogue. Coordinates and geometry are deliberately
-- null/absent until a trustworthy map source is available.
insert into public.routes (id, name, direction, active, service_notice)
values
  ('10000000-0000-0000-0000-000000000002', 'Rute Keliling Gading Serpong', 'Gading Serpong (loop)', true, 'Data peta belum tersedia. Waktu tiba dapat berubah sesuai kondisi kepadatan lalu lintas.'),
  ('10000000-0000-0000-0000-000000000003', 'Grogol ke UMN', 'Grogol → UMN', true, 'Data peta belum tersedia.'),
  ('10000000-0000-0000-0000-000000000004', 'UMN ke Grogol', 'UMN → Grogol', true, 'Data peta belum tersedia. Waktu tiba dapat berubah sesuai kondisi kepadatan lalu lintas.'),
  ('10000000-0000-0000-0000-000000000005', 'Tangerang City ke UMN/MNP', 'Tangerang City → UMN/MNP', true, 'Data peta belum tersedia.'),
  ('10000000-0000-0000-0000-000000000006', 'UMN ke Tangerang City', 'UMN → Tangerang City', true, 'Data peta belum tersedia. Waktu tiba dapat berubah sesuai kondisi kepadatan lalu lintas.'),
  ('10000000-0000-0000-0000-000000000007', 'Intermoda Cisauk ke UMN', 'Intermoda Cisauk → UMN', true, 'Data peta belum tersedia.'),
  ('10000000-0000-0000-0000-000000000008', 'UMN ke Intermoda Cisauk', 'UMN → Intermoda Cisauk', true, 'Data peta belum tersedia. Waktu tiba dapat berubah sesuai kondisi kepadatan lalu lintas.')
on conflict (id) do update set
  name = excluded.name,
  direction = excluded.direction,
  active = excluded.active,
  service_notice = excluded.service_notice;

insert into public.stops (id, route_id, name, sequence, latitude, longitude)
values
  ('12000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000002', 'Kampus UMN', 1, null, null),
  ('12000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002', 'SMS – Bundaran', 2, null, null),
  ('12000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000002', 'McDonald GS', 3, null, null),
  ('12000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000002', 'Ruko Boulevard', 4, null, null),
  ('12000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000002', 'Dormitory UMN', 5, null, null),
  ('12000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000002', 'Assobirin', 6, null, null),
  ('12000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000002', 'Bundaran Alam Sutera', 7, null, null),
  ('12000000-0000-0000-0000-000000000008', '10000000-0000-0000-0000-000000000002', 'Villa Melati Mas', 8, null, null),
  ('12000000-0000-0000-0000-000000000009', '10000000-0000-0000-0000-000000000002', 'BSD Plaza', 9, null, null),
  ('12000000-0000-0000-0000-000000000010', '10000000-0000-0000-0000-000000000002', 'Halte Pasar Modern BSD', 10, null, null),
  ('13000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000003', 'Grogol', 1, null, null),
  ('13000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000003', 'Green Garden Pesing', 2, null, null),
  ('13000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003', 'Halte Pasar Pisang/Bojong Cengkareng', 3, null, null),
  ('13000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000003', 'Gerbang Tol Puri Kembangan', 4, null, null),
  ('13000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000003', 'Apartemen Metro Garden', 5, null, null),
  ('13000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000003', 'Rest Area KM 13,5', 6, null, null),
  ('13000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000003', 'UMN', 7, null, null),
  ('14000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000004', 'UMN', 1, null, null),
  ('14000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000004', 'Rest Area Karang Tengah KM 14', 2, null, null),
  ('14000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000004', 'Apartemen Metro Garden', 3, null, null),
  ('14000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000004', 'Gerbang Tol Puri Kembangan', 4, null, null),
  ('14000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000004', 'Halte Pasar Pisang/Bojong Cengkareng', 5, null, null),
  ('14000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000004', 'Green Garden Pesing', 6, null, null),
  ('14000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000004', 'Grogol', 7, null, null),
  ('15000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000005', 'Halte Total Depan Tang City', 1, null, null),
  ('15000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000005', 'Depan Transmart Cikokol', 2, null, null),
  ('15000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000005', 'Setos Kebon Nanas', 3, null, null),
  ('15000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000005', 'PT Tiffico', 4, null, null),
  ('15000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000005', 'Gerbang Gading Serpong', 5, null, null),
  ('15000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000005', 'Dormitory', 6, null, null),
  ('15000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000005', 'UMN', 7, null, null),
  ('15000000-0000-0000-0000-000000000008', '10000000-0000-0000-0000-000000000005', 'MNP', 8, null, null),
  ('16000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000006', 'UMN', 1, null, null),
  ('16000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000006', 'Dormitory', 2, null, null),
  ('16000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000006', 'Gerbang Gading Serpong', 3, null, null),
  ('16000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000006', 'Halte Seberang Transmart Cikokol', 4, null, null),
  ('16000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000006', 'Halte Total Depan Tang City', 5, null, null),
  ('17000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000007', 'Intermoda Cisauk', 1, null, null),
  ('17000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000007', 'UMN', 2, null, null),
  ('18000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000008', 'UMN', 1, null, null),
  ('18000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000008', 'Intermoda Cisauk', 2, null, null)
on conflict (id) do update set
  route_id = excluded.route_id,
  name = excluded.name,
  sequence = excluded.sequence,
  latitude = excluded.latitude,
  longitude = excluded.longitude;

insert into public.route_timetable_entries (route_id, sequence, period, departure_time, description)
values
  ('10000000-0000-0000-0000-000000000002', 1, 'Rute keliling', '09.15', 'Kampus UMN'),
  ('10000000-0000-0000-0000-000000000002', 2, 'Rute keliling', '10.40', 'SMS – Bundaran'),
  ('10000000-0000-0000-0000-000000000002', 3, 'Rute keliling', '12.15', 'McDonald GS'),
  ('10000000-0000-0000-0000-000000000002', 4, 'Rute keliling', '14.15', 'Ruko Boulevard – Dormitory UMN – Kampus UMN'),
  ('10000000-0000-0000-0000-000000000002', 5, 'Rute keliling', '17.30', 'UMN – Dormitory – Assobirin – Bundaran Alam Sutera – Villa Melati Mas – BSD Plaza – Halte Pasar Modern BSD'),
  ('10000000-0000-0000-0000-000000000003', 1, 'Pagi', '05.25–05.47', 'Grogol – Green Garden Pesing'),
  ('10000000-0000-0000-0000-000000000003', 2, 'Pagi', '05.47–05.54', 'Green Garden Pesing – Halte Pasar Pisang/Bojong Cengkareng'),
  ('10000000-0000-0000-0000-000000000003', 3, 'Pagi', '05.54–06.07', 'Halte Pasar Pisang/Bojong Cengkareng – Gerbang Tol Puri Kembangan'),
  ('10000000-0000-0000-0000-000000000003', 4, 'Pagi', '06.07–06.17', 'Gerbang Tol Puri Kembangan – Apartemen Metro Garden'),
  ('10000000-0000-0000-0000-000000000003', 5, 'Pagi', '06.17–06.27', 'Apartemen Metro Garden – Rest Area KM 13,5'),
  ('10000000-0000-0000-0000-000000000003', 6, 'Pagi', '06.27–07.00', 'Rest Area KM 13,5 – UMN'),
  ('10000000-0000-0000-0000-000000000004', 1, 'Sore', '17.15–18.15', 'UMN – Rest Area Karang Tengah KM 14 – Apartemen Metro Garden – Gerbang Tol Puri Kembangan'),
  ('10000000-0000-0000-0000-000000000004', 2, 'Sore', '18.15–18.35', 'Gerbang Tol Puri Kembangan – Apartemen Metro Garden'),
  ('10000000-0000-0000-0000-000000000004', 3, 'Sore', '18.35–18.55', 'Halte Pasar Pisang/Bojong Cengkareng – Green Garden Pesing'),
  ('10000000-0000-0000-0000-000000000004', 4, 'Sore', '18.55–19.15', 'Green Garden Pesing – Grogol'),
  ('10000000-0000-0000-0000-000000000005', 1, 'Pagi', '06.40', 'Halte Total Depan Tang City – Depan Transmart Cikokol – Setos Kebon Nanas – PT Tiffico – Gerbang Gading Serpong'),
  ('10000000-0000-0000-0000-000000000005', 2, 'Pagi', '07.15', 'Dormitory – UMN – MNP'),
  ('10000000-0000-0000-0000-000000000005', 3, 'Pagi', '07.25', 'Dormitory – UMN – MNP'),
  ('10000000-0000-0000-0000-000000000005', 4, 'Pagi', '07.35', 'Tiba UMN'),
  ('10000000-0000-0000-0000-000000000005', 5, 'Pagi', '07.40', 'Tiba MNP'),
  ('10000000-0000-0000-0000-000000000006', 1, 'Siang', '13.00', 'UMN'),
  ('10000000-0000-0000-0000-000000000006', 2, 'Siang', '13.15', 'Dormitory – Gerbang Gading Serpong – Halte Seberang Transmart Cikokol – Halte Total Depan Tang City'),
  ('10000000-0000-0000-0000-000000000006', 3, 'Sore', '16.00', 'Dormitory'),
  ('10000000-0000-0000-0000-000000000006', 4, 'Sore', '16.30', 'Tiba UMN'),
  ('10000000-0000-0000-0000-000000000006', 5, 'Sore', '17.30', 'UMN – Dormitory – Gerbang Gading Serpong – Halte Seberang Transmart Cikokol – Halte Total Depan Tang City'),
  ('10000000-0000-0000-0000-000000000007', 1, 'Pagi', '07.00', 'Intermoda Cisauk – UMN'),
  ('10000000-0000-0000-0000-000000000008', 1, 'Sore', '17.45', 'UMN – Intermoda Cisauk')
on conflict (route_id, sequence) do update set
  period = excluded.period,
  departure_time = excluded.departure_time,
  description = excluded.description;
