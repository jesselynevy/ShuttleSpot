begin;

select plan(17);

select is(
  public.hook_restrict_student_signup(
    '{"user":{"phone":"+628123456789"}}'::jsonb
  ) ? 'error',
  true,
  'public phone signup is rejected'
);

select is(
  public.hook_restrict_student_signup(
    '{"user":{"email":"driver.628100000001@shuttlespot.internal","app_metadata":{"provisioned_role":"driver","display_name":"Driver One","phone_e164":"+628100000001"}}}'::jsonb
  ) ? 'error',
  false,
  'trusted driver email alias is accepted'
);

insert into auth.users (
  id, instance_id, aud, role, email, phone, encrypted_password,
  email_confirmed_at, phone_confirmed_at, raw_app_meta_data, raw_user_meta_data,
  created_at, updated_at
)
values
  (
    'a0000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000000',
    'authenticated', 'authenticated', 'driver.628100000001@shuttlespot.internal', null,
    crypt('Temporary1', gen_salt('bf')), now(), null,
    '{"provisioned_role":"driver","display_name":"Driver One","phone_e164":"+628100000001"}'::jsonb,
    '{}'::jsonb, now(), now()
  ),
  (
    'a0000000-0000-0000-0000-000000000002',
    '00000000-0000-0000-0000-000000000000',
    'authenticated', 'authenticated', 'driver.628100000002@shuttlespot.internal', null,
    crypt('Temporary1', gen_salt('bf')), now(), null,
    '{"provisioned_role":"driver","display_name":"Driver Two","phone_e164":"+628100000002"}'::jsonb,
    '{}'::jsonb, now(), now()
  ),
  (
    'a0000000-0000-0000-0000-000000000003',
    '00000000-0000-0000-0000-000000000000',
    'authenticated', 'authenticated', 'driver.rpc.passenger@student.umn.ac.id', null,
    crypt('Password1', gen_salt('bf')), now(), null,
    '{}'::jsonb,
    '{"display_name":"Passenger"}'::jsonb, now(), now()
  );

select is(
  (
    select must_change_password
    from public.profiles
    where id = 'a0000000-0000-0000-0000-000000000001'
  ),
  true,
  'provisioned driver must change the temporary password'
);

insert into public.routes (id, name, direction, active)
values ('a1000000-0000-0000-0000-000000000001', 'Driver Test', 'A → B', true);

insert into public.stops (id, route_id, name, sequence, latitude, longitude)
values
  ('a2000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000001', 'A', 1, null, null),
  ('a2000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000001', 'B', 2, null, null);

insert into public.vehicles (id, plate_number, capacity, active, label)
values ('a3000000-0000-0000-0000-000000000001', 'DRIVER-RPC', null, true, 'Driver RPC Bus');

insert into public.trips (
  id, route_id, vehicle_id, driver_id, status, current_stop_sequence
)
values (
  'a4000000-0000-0000-0000-000000000001',
  'a1000000-0000-0000-0000-000000000001',
  'a3000000-0000-0000-0000-000000000001',
  'a0000000-0000-0000-0000-000000000001',
  'not_started',
  0
);

set local role authenticated;
select set_config(
  'request.jwt.claims',
  '{"sub":"a0000000-0000-0000-0000-000000000001","role":"authenticated"}',
  true
);

select throws_ok(
  $$select public.start_assigned_trip('a4000000-0000-0000-0000-000000000001')$$,
  '42501',
  'Active driver with changed password required',
  'driver cannot start before changing the temporary password'
);

reset role;
update auth.users
set encrypted_password = crypt('ChangedPassword1', gen_salt('bf'))
where id = 'a0000000-0000-0000-0000-000000000001';

select is(
  (
    select must_change_password
    from public.profiles
    where id = 'a0000000-0000-0000-0000-000000000001'
  ),
  false,
  'password update clears the first-login requirement'
);

set local role authenticated;
select set_config(
  'request.jwt.claims',
  '{"sub":"a0000000-0000-0000-0000-000000000002","role":"authenticated"}',
  true
);

select throws_ok(
  $$select public.start_assigned_trip('a4000000-0000-0000-0000-000000000001')$$,
  '42501',
  'Active driver with changed password required',
  'another driver without a changed password cannot operate the trip'
);

reset role;
update auth.users
set encrypted_password = crypt('ChangedPassword1', gen_salt('bf'))
where id = 'a0000000-0000-0000-0000-000000000002';

set local role authenticated;
select set_config(
  'request.jwt.claims',
  '{"sub":"a0000000-0000-0000-0000-000000000002","role":"authenticated"}',
  true
);

select throws_ok(
  $$select public.start_assigned_trip('a4000000-0000-0000-0000-000000000001')$$,
  '42501',
  'Assigned trip not found',
  'driver cannot start another driver trip'
);

reset role;
set local role authenticated;
select set_config(
  'request.jwt.claims',
  '{"sub":"a0000000-0000-0000-0000-000000000001","role":"authenticated"}',
  true
);

select lives_ok(
  $$select public.get_assigned_trip()$$,
  'ready driver can load the assigned trip'
);

select lives_ok(
  $$select public.start_assigned_trip('a4000000-0000-0000-0000-000000000001')$$,
  'assigned driver can start the trip'
);

select throws_ok(
  $$select public.start_assigned_trip('a4000000-0000-0000-0000-000000000001')$$,
  '23514',
  'Trip can only start once',
  'trip cannot start twice'
);

select lives_ok(
  $$select public.advance_trip_stop('a4000000-0000-0000-0000-000000000001')$$,
  'active trip advances one stop'
);

select is(
  (
    select current_stop_sequence
    from public.trips
    where id = 'a4000000-0000-0000-0000-000000000001'
  ),
  1,
  'advance changes the stop sequence by one'
);

select throws_ok(
  $$select public.finish_assigned_trip('a4000000-0000-0000-0000-000000000001')$$,
  '23514',
  'Reach the final stop before finishing the trip',
  'trip cannot finish before the final stop'
);

select lives_ok(
  $$select public.advance_trip_stop('a4000000-0000-0000-0000-000000000001')$$,
  'active trip reaches the final stop'
);

select lives_ok(
  $$select public.finish_assigned_trip('a4000000-0000-0000-0000-000000000001')$$,
  'trip can finish at the final stop'
);

select throws_ok(
  $$select public.finish_assigned_trip('a4000000-0000-0000-0000-000000000001')$$,
  '23514',
  'Only an active trip can finish',
  'completed trip cannot finish twice'
);

select is(
  has_table_privilege('authenticated', 'public.trips', 'UPDATE'),
  false,
  'drivers cannot bypass RPC validation with direct updates'
);

select * from finish();
rollback;
