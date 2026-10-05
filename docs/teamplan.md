# Team-ready ShuttleSpot base implementation

## Summary

Build the base in the existing single `app` module, organized by `core`, `data`, `feature`, and `navigation` packages. It should give every teammate stable screen contracts, fake demo data, Supabase/Room foundations, and navigation without requiring them to change shared app wiring.

A **fake adapter** is an in-memory implementation of a repository interface. It returns deterministic demo data now, while a Supabase adapter can later replace it without changing ViewModels or Compose screens.

## Base architecture

- Keep Compose screens passive: each feature owns a `ViewModel`, immutable `UiState`, and sealed `Event`; composables render state and send events only.
- Replace generic placeholders with role-aware navigation graphs: shared onboarding, passenger graph, and driver graph. Guard role-only destinations through the session state rather than direct UI navigation.
- Keep `core/model` as pure Kotlin models. Add shared models for `UserRole`, `Session`, `RouteSelection`, `ActiveTripSnapshot`, `LiveLocation`, `EtaEstimate`, `BoardingIntent`, and `TripStatus`.
- Establish four deep repository modules with small interfaces:
    - `SessionRepository`: observe session, passenger email-gate sign-in, registered-driver phone lookup, sign-out.
    - `RouteRepository`: browse routes/stops, inspect route detail, observe/save primary selection.
    - `PassengerTripRepository`: observe selected-route trip and submit/cancel one boarding intent.
    - `DriverTripRepository`: observe assigned trip, start trip, confirm stop progress, finish trip.
- Add a `LocationPublisher` module that hides runtime permission, fused location updates, foreground-service lifecycle, and retry behavior behind `start(tripId)` and `stop()`.

## Data foundation

- Retain the current in-memory route repository as the default fake adapter and add equivalent deterministic fake adapters for session, passenger trip, and driver trip. Seed scenarios for no active bus, en route, arrived, missed, completed, no assigned trip, and active driver trip.
- Bind fake adapters through Hilt for local development. Feature code must depend only on repository interfaces, never directly on Supabase, Room, Maps, or Android location APIs.
- Add Supabase configuration validation around the existing untracked `SUPABASE_URL` and `SUPABASE_ANON_KEY`; provide a credential-free example file and document setup. Never commit real values.
- Add versioned Supabase migrations for `profiles`, `passenger_preferences`, `routes`, `stops`, `vehicles`, `trips`, `vehicle_locations`, `boarding_intents`, `announcements`, and `notification_subscriptions`. Include UUID keys, required indexes, foreign keys, and RLS policies matching passenger/driver permissions.
- Add Room entities/DAOs for cached route data, primary selection, latest trip snapshot, announcements, and a generic outbox. Define idempotent outbox operation types for route selection and boarding intent; reserve driver GPS syncing for the driver feature.
- Keep a narrow remote seam: Supabase adapters use the same repository interfaces as fake adapters. Teammates can replace one adapter at a time without altering a screen.

## Shared UI and feature handoff

- Finish reusable Material 3 infrastructure: app scaffold, passenger/driver bottom-navigation models, loading/empty/error/offline states, `TripStatusCard`, route-progress display, last-updated label, and confirmation dialog.
- Implement the complete navigation contract and screen-level state shells for onboarding, passenger route setup, passenger Home, Live Tracker, boarding confirmation, driver sign-in, Driver Home, and Driver Active Trip.
- Make only the shared base functional with fake data: email-domain gate, registered-phone lookup, saved primary-route selection, Home state switching, and role-gated navigation.
- Leave feature-specific behavior behind defined events so ownership is clear:
    - Passenger teammate: route picker, Home/Live Tracker, boarding intent.
    - Driver teammate: assigned trip, GPS publisher, stop progression, finish trip.
    - Backend/offline teammate: Supabase adapters, Room, outbox, WorkManager, Realtime, and FCM.
- Preserve the existing dirty worktree; do not reset or overwrite unrelated changes.

## Test plan

- Keep the passing `testDebugUnitTest` baseline, then add repository-contract tests run against fake adapters.
- Add ViewModel tests for invalid passenger email, registered/unregistered driver number, route-selection validation, no-bus/active/stale trip states, and duplicate boarding-intent prevention.
- Add navigation/Compose tests proving each role cannot enter the other role’s protected destinations.
- Add Hilt graph and Room migration tests. Run Supabase migrations only against the configured non-production project or local Supabase instance.
- Base acceptance: a teammate can add a feature by implementing one repository adapter or one feature package without editing the shared navigation host, existing feature ViewModels, or another teammate’s screen.

## Assumptions

- The initial base uses fake adapters by default so development can proceed before Supabase credentials or live UMN data are available.
- Passenger sign-in remains an email-domain demo gate; driver sign-in remains a pre-registered phone lookup with no OTP.
- GPS, Realtime, WorkManager sync, and FCM are scaffolded by interfaces/configuration first; their production behavior is implemented in the corresponding feature slices after the base is merged.
