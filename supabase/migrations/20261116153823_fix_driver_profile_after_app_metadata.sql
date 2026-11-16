-- Supabase Admin API writes app_metadata after the initial auth.users insert.
-- Driver profile creation must therefore also react to that metadata update.

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
  expected_driver_alias text;
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

    expected_driver_alias :=
      'driver.'
      || substring(normalized_phone from 2)
      || '@shuttlespot.internal';

    if normalized_email <> expected_driver_alias then
      raise exception 'Driver account alias does not match phone number'
        using errcode = '23514';
    end if;

    insert into public.profiles (
      id,
      role,
      display_name,
      email,
      phone_e164,
      active,
      must_change_password
    )
    values (
      new.id,
      'driver'::public.user_role,
      driver_display_name,
      null,
      normalized_phone,
      true,
      true
    )
    on conflict (id) do nothing;

    return new;
  end if;

  -- The Admin API has inserted the internal driver alias, but it has not
  -- written app_metadata yet. Profile creation continues in the update trigger.
  if tg_op = 'INSERT'
     and normalized_email ~ '^driver\.628[0-9]{8,12}@shuttlespot\.internal$' then
    return new;
  end if;

  -- Metadata updates without the trusted driver marker need no profile work.
  if tg_op = 'UPDATE' then
    return new;
  end if;

  if normalized_email is null
     or split_part(normalized_email, '@', 1) = ''
     or split_part(normalized_email, '@', 2) <> 'student.umn.ac.id'
     or normalized_email <>
        split_part(normalized_email, '@', 1)
        || '@'
        || split_part(normalized_email, '@', 2) then
    raise exception 'Passenger profile requires an @student.umn.ac.id email'
      using errcode = '23514';
  end if;

  insert into public.profiles (
    id,
    role,
    display_name,
    email,
    phone_e164,
    active,
    must_change_password
  )
  values (
    new.id,
    'passenger'::public.user_role,
    coalesce(
      passenger_display_name,
      split_part(normalized_email, '@', 1)
    ),
    normalized_email,
    null,
    true,
    false
  )
  on conflict (id) do nothing;

  return new;
end;
$$;

revoke all on function public.handle_new_profile()
  from public, anon, authenticated;

drop trigger if exists on_auth_profile_created on auth.users;

create trigger on_auth_profile_created
after insert on auth.users
for each row
execute function public.handle_new_profile();

drop trigger if exists on_auth_profile_metadata_updated on auth.users;

create trigger on_auth_profile_metadata_updated
after update of raw_app_meta_data on auth.users
for each row
when (
  old.raw_app_meta_data is distinct from new.raw_app_meta_data
)
execute function public.handle_new_profile();