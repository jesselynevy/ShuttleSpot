-- Promote the seven remaining prototype routes into the hosted development
-- catalogue. This is a migration because hosted db push records seed hashes but
-- does not re-run an already-applied seed file when its contents change.

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

with route_stops(route_id, id_prefix, names) as (
  values
    ('10000000-0000-0000-0000-000000000002'::uuid, '12', array['Kampus UMN', 'SMS – Bundaran', 'McDonald GS', 'Ruko Boulevard', 'Dormitory UMN', 'Assobirin', 'Bundaran Alam Sutera', 'Villa Melati Mas', 'BSD Plaza', 'Halte Pasar Modern BSD']),
    ('10000000-0000-0000-0000-000000000003'::uuid, '13', array['Grogol', 'Green Garden Pesing', 'Halte Pasar Pisang/Bojong Cengkareng', 'Gerbang Tol Puri Kembangan', 'Apartemen Metro Garden', 'Rest Area KM 13,5', 'UMN']),
    ('10000000-0000-0000-0000-000000000004'::uuid, '14', array['UMN', 'Rest Area Karang Tengah KM 14', 'Apartemen Metro Garden', 'Gerbang Tol Puri Kembangan', 'Halte Pasar Pisang/Bojong Cengkareng', 'Green Garden Pesing', 'Grogol']),
    ('10000000-0000-0000-0000-000000000005'::uuid, '15', array['Halte Total Depan Tang City', 'Depan Transmart Cikokol', 'Setos Kebon Nanas', 'PT Tiffico', 'Gerbang Gading Serpong', 'Dormitory', 'UMN', 'MNP']),
    ('10000000-0000-0000-0000-000000000006'::uuid, '16', array['UMN', 'Dormitory', 'Gerbang Gading Serpong', 'Halte Seberang Transmart Cikokol', 'Halte Total Depan Tang City']),
    ('10000000-0000-0000-0000-000000000007'::uuid, '17', array['Intermoda Cisauk', 'UMN']),
    ('10000000-0000-0000-0000-000000000008'::uuid, '18', array['UMN', 'Intermoda Cisauk'])
), expanded as (
  select
    route_id,
    (id_prefix || '000000-0000-0000-0000-' || lpad(stop_number::text, 12, '0'))::uuid as stop_id,
    stop_name,
    stop_number::integer as sequence
  from route_stops
  cross join lateral unnest(names) with ordinality as stop(stop_name, stop_number)
)
insert into public.stops (id, route_id, name, sequence, latitude, longitude)
select stop_id, route_id, stop_name, sequence, null, null
from expanded
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
