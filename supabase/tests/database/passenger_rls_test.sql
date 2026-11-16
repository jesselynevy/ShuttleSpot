begin;

select plan(11);

-- Stable fixtures owned by this transaction; rollback keeps every environment clean.
insert into auth.users (
  id, instance_id, aud, role, email, encrypted_password, email_confirmed_at,
  raw_app_meta_data, raw_user_meta_data, created_at, updated_at
)
values
  ('90000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000000', 'authenticated', 'authenticated', 'rls.passenger1@student.umn.ac.id', crypt('Password1', gen_salt('bf')), now(), '{}'::jsonb, '{"display_name":"Passenger One"}'::jsonb, now(), now()),
  ('90000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000000', 'authenticated', 'authenticated', 'rls.passenger2@student.umn.ac.id', crypt('Password1', gen_salt('bf')), now(), '{}'::jsonb, '{"display_name":"Passenger Two"}'::jsonb, now(), now()),
  ('90000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000000', 'authenticated', 'authenticated', 'rls.driver1@student.umn.ac.id', crypt('Password1', gen_salt('bf')), now(), '{}'::jsonb, '{"display_name":"Driver One"}'::jsonb, now(), now()),
  ('90000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000000', 'authenticated', 'authenticated', 'rls.driver2@student.umn.ac.id', crypt('Password1', gen_salt('bf')), now(), '{}'::jsonb, '{"display_name":"Driver Two"}'::jsonb, now(), now());

update public.profiles
set role = 'driver'::public.user_role,
    email = null,
    phone_e164 = case id
      when '90000000-0000-0000-0000-000000000003'::uuid then '+628000000003'
      else '+628000000004'
    end
where id in (
  '90000000-0000-0000-0000-000000000003'::uuid,
  '90000000-0000-0000-0000-000000000004'::uuid
);

insert into public.routes (id, name, direction, active)
values ('91000000-0000-0000-0000-000000000001', 'RLS Test Route', 'A → B', true);

insert into public.stops (id, route_id, name, sequence, latitude, longitude)
values
  ('92000000-0000-0000-0000-000000000001', '91000000-0000-0000-0000-000000000001', 'A', 1, null, null),
  ('92000000-0000-0000-0000-000000000002', '91000000-0000-0000-0000-000000000001', 'B', 2, null, null);

insert into public.vehicles (id, plate_number, capacity, active, label)
values ('93000000-0000-0000-0000-000000000001', 'RLS-TEST', 20, true, 'RLS Test Bus');

insert into public.trips (id, route_id, vehicle_id, driver_id, status)
values
  ('94000000-0000-0000-0000-000000000001', '91000000-0000-0000-0000-000000000001', '93000000-0000-0000-0000-000000000001', '90000000-0000-0000-0000-000000000003', 'active'),
  ('94000000-0000-0000-0000-000000000002', '91000000-0000-0000-0000-000000000001', '93000000-0000-0000-0000-000000000001', '90000000-0000-0000-0000-000000000004', 'active');

set local role authenticated;
select set_config('request.jwt.claims', '{"sub":"90000000-0000-0000-0000-000000000001","role":"authenticated"}', true);

select is(
  (select count(*) from public.profiles),
  1::bigint,
  'passenger reads only their own profile'
);

select is(
  has_table_privilege('authenticated', 'public.profiles', 'UPDATE'),
  false,
  'passenger cannot update profile role'
);

select lives_ok(
  $$select public.save_passenger_preference(
    '91000000-0000-0000-0000-000000000001',
    '92000000-0000-0000-0000-000000000001',
    '92000000-0000-0000-0000-000000000002'
  )$$,
  'passenger can save their own valid preference through the RPC'
);

select lives_ok(
  $$select public.submit_boarding_intent(
    '94000000-0000-0000-0000-000000000001',
    '92000000-0000-0000-0000-000000000001',
    '95000000-0000-0000-0000-000000000001'
  )$$,
  'passenger can submit a boarding intent through the RPC'
);

select lives_ok(
  $$select public.submit_boarding_intent(
    '94000000-0000-0000-0000-000000000001',
    '92000000-0000-0000-0000-000000000001',
    '95000000-0000-0000-0000-000000000001'
  )$$,
  'retrying the same boarding intent is idempotent'
);

reset role;

select is(
  (select count(*) from public.boarding_intents where idempotency_key = '95000000-0000-0000-0000-000000000001'),
  1::bigint,
  'duplicate boarding retry creates only one row'
);

set local role authenticated;
select set_config('request.jwt.claims', '{"sub":"90000000-0000-0000-0000-000000000002","role":"authenticated"}', true);

select is(
  (select count(*) from public.passenger_preferences),
  0::bigint,
  'passenger cannot read another passenger preference'
);

select is(
  (select count(*) from public.boarding_intents),
  0::bigint,
  'passenger cannot read another passenger boarding intent'
);

reset role;
set local role authenticated;
select set_config('request.jwt.claims', '{"sub":"90000000-0000-0000-0000-000000000004","role":"authenticated"}', true);

select is(
  (
    select count(*)
    from public.trips
    where id = '94000000-0000-0000-0000-000000000001'
  ),
  0::bigint,
  'driver cannot read a trip assigned to another driver'
);

select is(
  (
    with changed as (
      update public.trips
      set current_stop_sequence = 1
      where id = '94000000-0000-0000-0000-000000000001'
      returning id
    )
    select count(*) from changed
  ),
  0::bigint,
  'driver cannot update a trip assigned to another driver'
);

reset role;

select is(
  public.hook_restrict_student_signup(
    '{"user":{"email":"outsider@gmail.com"}}'::jsonb
  ) ? 'error',
  true,
  'non-student signup domain is rejected'
);

select * from finish();
rollback;
