-- Restricts public passenger signup to UMN student email addresses, creates the
-- corresponding application profile, and prevents clients from changing roles.

create or replace function public.hook_restrict_student_signup(event jsonb)
returns jsonb
language plpgsql
stable
set search_path = ''
as $$
declare
  user_email text := lower(btrim(event->'user'->>'email'));
  local_part text;
  domain_part text;
begin
  local_part := split_part(coalesce(user_email, ''), '@', 1);
  domain_part := split_part(coalesce(user_email, ''), '@', 2);

  if user_email is null
     or local_part = ''
     or domain_part <> 'student.umn.ac.id'
     or user_email <> local_part || '@' || domain_part then
    return jsonb_build_object(
      'error', jsonb_build_object(
        'http_code', 400,
        'message', 'Gunakan email mahasiswa dengan domain @student.umn.ac.id.'
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

create or replace function public.handle_new_passenger_profile()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare
  normalized_email text := lower(btrim(new.email));
  requested_display_name text := nullif(btrim(new.raw_user_meta_data->>'display_name'), '');
begin
  if normalized_email is null
     or split_part(normalized_email, '@', 1) = ''
     or split_part(normalized_email, '@', 2) <> 'student.umn.ac.id'
     or normalized_email <> split_part(normalized_email, '@', 1) || '@' || split_part(normalized_email, '@', 2) then
    raise exception 'Passenger profile requires an @student.umn.ac.id email'
      using errcode = '23514';
  end if;

  insert into public.profiles (
    id,
    role,
    display_name,
    email
  )
  values (
    new.id,
    'passenger'::public.user_role,
    coalesce(requested_display_name, split_part(normalized_email, '@', 1)),
    normalized_email
  );

  return new;
end;
$$;

revoke all on function public.handle_new_passenger_profile()
  from public, anon, authenticated;

drop trigger if exists on_auth_passenger_created on auth.users;
create trigger on_auth_passenger_created
after insert on auth.users
for each row execute function public.handle_new_passenger_profile();

drop policy if exists "manage own profile" on public.profiles;
drop policy if exists "read own profile" on public.profiles;

create policy "read own profile"
on public.profiles
for select
to authenticated
using (id = auth.uid());

revoke all on table public.profiles from anon;
revoke insert, update, delete on table public.profiles from authenticated;
grant select on table public.profiles to authenticated;
