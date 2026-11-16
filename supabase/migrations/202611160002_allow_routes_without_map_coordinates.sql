-- Some verified UMN route catalog entries have stops and timetables but do not
-- yet have trustworthy map coordinates. Keep those coordinates null instead of
-- inventing points; the Android map treats missing geometry as unavailable.

alter table public.stops
  alter column latitude drop not null,
  alter column longitude drop not null;
