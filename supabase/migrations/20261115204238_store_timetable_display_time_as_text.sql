alter table public.route_timetable_entries
alter column departure_time type text
using to_char(departure_time, 'HH24:MI');