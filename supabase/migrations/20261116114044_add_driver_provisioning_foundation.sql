-- Driver account state and trusted provisioning boundary.

alter table public.profiles
  add column active boolean not null default true,
  add column must_change_password boolean not null default false;

alter table public.profiles
  add constraint profiles_password_change_driver_only
  check (
    role = 'driver'::public.user_role
    or not must_change_password
  );

alter table public.vehicles
  alter column capacity drop not null;

create or replace function public.hook_restrict_student_signup(event jsonb)
returns jsonb
language plpgsql
stable
set search_path = ''
as $$
declare
  user_email text := lower(nullif(btrim(event->'user'->>'email'), ''));
  user_phone text := nullif(btrim(event->'user'->>'phone'), '');
  provisioned_role text := lower(nullif(
    btrim(event->'user'->'app_metadata'->>'provisioned_role'),
    ''
  ));
  provisioned_display_name text := nullif(
    btrim(event->'user'->'app_metadata'->>'display_name'),
    ''
  );
  provisioned_phone text := nullif(
    btrim(event->'user'->'app_metadata'->>'phone_e164'),
    ''
  );
  expected_driver_alias text;
  local_part text;
  domain_part text;
begin
  -- Only the trusted Admin API can place provisioned_role in app_metadata.
  if provisioned_role = 'driver' then
    if provisioned_phone is null
       or provisioned_phone !~ '^\+628[0-9]{8,12}$' then
      return jsonb_build_object(
        'error',
        jsonb_build_object(
          'http_code', 400,
          'message', 'Driver requires a normalized Indonesian phone number.'
        )
      );
    end if;

    if provisioned_display_name is null then
      return jsonb_build_object(
        'error',
        jsonb_build_object(
          'http_code', 400,
          'message', 'Driver provisioning requires a display name.'
        )
      );
    end if;

    expected_driver_alias := 'driver.'
      || substring(provisioned_phone from 2)
      || '@shuttlespot.internal';

    if user_email <> expected_driver_alias or user_phone is not null then
      return jsonb_build_object(
        'error',
        jsonb_build_object(
          'http_code', 400,
          'message', 'Driver account alias is invalid.'
        )
      );
    end if;

    return '{}'::jsonb;
  end if;

  if provisioned_role is not null then
    return jsonb_build_object(
      'error',
      jsonb_build_object(
        'http_code', 403,
        'message', 'Unsupported provisioned role.'
      )
    );
  end if;

  local_part := split_part(coalesce(user_email, ''), '@', 1);
  domain_part := split_part(coalesce(user_email, ''), '@', 2);

  if user_email is null
     or local_part = ''
     or domain_part <> 'student.umn.ac.id'
     or user_email <> local_part || '@' || domain_part then
    return jsonb_build_object(
      'error',
      jsonb_build_object(
        'http_code', 400,
        'message',
        'Gunakan email mahasiswa dengan domain @student.umn.ac.id.'
      )
    );
  end if;

  return '{}'::jsonb;
end;
$$;

revoke all on function public.hook_restrict_student_signup(jsonb)
  from public, anon, authenticated;
grant usage on schema public to supabase_auth_admin;
grant execute on function public.hook_restrict_student_signup(jsonb)
  to supabase_auth_admin;

create or replace function public.handle_new_profile()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare
  provisioned_role text := lower(nullif(
    btrim(new.raw_app_meta_data->>'provisioned_role'),
    ''
  ));
  normalized_email text := lower(nullif(btrim(new.email), ''));
  normalized_phone text := nullif(
    btrim(new.raw_app_meta_data->>'phone_e164'),
    ''
  );
  passenger_display_name text := nullif(
    btrim(new.raw_user_meta_data->>'display_name'),
    ''
  );
  driver_display_name text := nullif(
    btrim(new.raw_app_meta_data->>'display_name'),
    ''
  );
begin
  if provisioned_role = 'driver' then
    if normalized_phone is null
       or normalized_phone !~ '^\+628[0-9]{8,12}$' then
      raise exception 'Driver profile requires a normalized phone number'
        using errcode = '23514';
    end if;

    if driver_display_name is null then
      raise exception 'Driver profile requires an administrator-provided name'
        using errcode = '23514';
    end if;

    insert into public.profiles (
      id, role, display_name, email, phone_e164, active, must_change_password
    ) values (
      new.id,
      'driver'::public.user_role,
      driver_display_name,
      null,
      normalized_phone,
      true,
      true
    );

    return new;
  end if;

  if normalized_email is null
     or split_part(normalized_email, '@', 1) = ''
     or split_part(normalized_email, '@', 2) <> 'student.umn.ac.id'
     or normalized_email <> split_part(normalized_email, '@', 1)
        || '@' || split_part(normalized_email, '@', 2) then
    raise exception 'Passenger profile requires an @student.umn.ac.id email'
      using errcode = '23514';
  end if;

  insert into public.profiles (
    id, role, display_name, email, phone_e164, active, must_change_password
  ) values (
    new.id,
    'passenger'::public.user_role,
    coalesce(passenger_display_name, split_part(normalized_email, '@', 1)),
    normalized_email,
    null,
    true,
    false
  );

  return new;
end;
$$;

revoke all on function public.handle_new_profile()
  from public, anon, authenticated;

drop trigger if exists on_auth_passenger_created on auth.users;
drop trigger if exists on_auth_profile_created on auth.users;
create trigger on_auth_profile_created
after insert on auth.users
for each row execute function public.handle_new_profile();

drop function if exists public.handle_new_passenger_profile();

create or replace function public.handle_driver_password_changed()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
  if old.encrypted_password is distinct from new.encrypted_password then
    update public.profiles
    set must_change_password = false
    where id = new.id
      and role = 'driver'::public.user_role;
  end if;

  return new;
end;
$$;

revoke all on function public.handle_driver_password_changed()
  from public, anon, authenticated;

drop trigger if exists on_auth_driver_password_changed on auth.users;
create trigger on_auth_driver_password_changed
after update of encrypted_password on auth.users
for each row execute function public.handle_driver_password_changed();

create or replace function public.current_driver_is_ready()
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
  select exists (
    select 1
    from public.profiles
    where profiles.id = auth.uid()
      and profiles.role = 'driver'::public.user_role
      and profiles.active
      and not profiles.must_change_password
  );
$$;

revoke all on function public.current_driver_is_ready()
  from public, anon, authenticated;
grant execute on function public.current_driver_is_ready()
  to authenticated;

drop policy if exists "drivers read assigned trips" on public.trips;
create policy "drivers read assigned trips"
on public.trips
for select
to authenticated
using (
  driver_id = auth.uid()
  and public.current_driver_is_ready()
);

drop policy if exists "driver updates assigned trip" on public.trips;
create policy "driver updates assigned trip"
on public.trips
for update
to authenticated
using (
  driver_id = auth.uid()
  and status in (
    'not_started'::public.trip_status,
    'active'::public.trip_status
  )
  and public.current_driver_is_ready()
)
with check (
  driver_id = auth.uid()
  and public.current_driver_is_ready()
);

drop policy if exists "driver inserts active location"
  on public.vehicle_locations;
create policy "driver inserts active location"
on public.vehicle_locations
for insert
to authenticated
with check (
  public.current_driver_is_ready()
  and exists (
    select 1
    from public.trips
    where trips.id = vehicle_locations.trip_id
      and trips.driver_id = auth.uid()
      and trips.status = 'active'::public.trip_status
  )
);

drop policy if exists "driver authors announcement"
  on public.announcements;
create policy "driver authors announcement"
on public.announcements
for insert
to authenticated
with check (
  author_id = auth.uid()
  and public.current_driver_is_ready()
);
