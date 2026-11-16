# Architecture

## Guiding decisions

Use a single Android app with role-gated navigation. Build a vertical slice for the passenger journey first, then the driver location publisher. The cloud is authoritative, while Room provides a usable local cache and an outbox for offline writes.

For the current passenger-first milestone, the approved Flow ShuttleSpot document defines the passenger journey. Student authentication, route catalogue refresh, and passenger preference/boarding outbox delivery have Supabase adapters. Live trip, location, Realtime, and notification delivery remain deterministic or deferred until their integrations are deliberately scheduled.

## Planned Android stack

| Concern | Choice |
| --- | --- |
| UI | Kotlin, Jetpack Compose, Material 3, Navigation Compose |
| State | ViewModels, `StateFlow`, unidirectional data flow |
| Local storage | Room database; DataStore for small preferences |
| Background sync | WorkManager with network constraint |
| Cloud | Supabase Auth, Postgres, Realtime, Edge Functions as needed |
| Maps/location | MapLibre Android + OpenStreetMap tiles; Fused Location Provider; foreground service for active driver trip |
| Notifications | Firebase Cloud Messaging |
| Dependency injection | Hilt |
| Tests | JUnit, Turbine/Coroutines tests, Compose UI tests |

## Layering

`feature UI -> ViewModel -> repository -> local Room + remote Supabase`

- Composables render state and emit events only.
- ViewModels own screen state and call use cases/repositories.
- Repositories expose local-first flows, refresh cloud data, and enqueue retryable mutations.
- The sync worker processes the outbox only with a network connection, using idempotency keys.

## Implemented foundation

- The current app remains a single Gradle module, organized into `core`, `data`, `feature`, and `navigation` packages.
- `SessionRepository`, `RouteRepository`, `PassengerTripRepository`, `DriverTripRepository`, and `NotificationRepository` are the public seams used by ViewModels. The app persists the non-secret local session, route catalogue/stops/geometry/timetables, primary route selection, latest trip snapshot, boarding intents, and personal notification inbox in Room. Static demo sources seed route and trip cache data until remote adapters are configured.
- `LocationPublisher` is an isolated seam; its current no-op adapter is intentionally replaced later by the driver foreground-service implementation.
- ConfettiKit provides the short Compose confetti animation on passenger registration completion.
- Room contains cache/outbox support for routes, stops, geometry, timetables, selections, trip snapshots, boarding intents, announcements, persisted sessions, personal notifications, and retryable passenger mutations. Route selection and boarding intents update Room first and enqueue a versioned, idempotent outbox mutation. In connected builds, network-constrained WorkManager work is enqueued immediately after a mutation and again on startup. Retryable failures stop ordered processing and retry at most five times; invalid payloads and rejected mutations are retained with a permanent-failure reason for diagnosis. Demo builds do not schedule remote sync.
- Passenger mutations call security-definer Postgres RPCs that derive ownership only from `auth.uid()`. Clients cannot supply a passenger/profile ID. Direct writes to preferences and boarding intents are revoked; RLS continues to scope reads to the owner. The boarding RPC validates the active trip and route stop and treats the idempotency key as a stable retry identity.
- `data.remote` is the only intended home for Supabase SDK DTOs and calls. UI and ViewModels consume domain models through repositories only.
- Supabase schema is versioned under `supabase/migrations/`; URL and anonymous-key values stay in untracked `local.properties` and are validated through `SupabaseConfig`.
- Passenger self-signup is restricted server-side by a Supabase Before User Created Postgres hook to the exact `student.umn.ac.id` domain. A database trigger creates the matching passenger profile, while authenticated clients can read only their own profile and cannot write role-bearing profile rows directly.
- With email confirmation enabled, Supabase returns an obfuscated user with no identities when an already-confirmed email signs up again. The auth data source maps that response to an explicit domain result; navigation returns the passenger to sign-in without waiting for an email that Supabase will not send.
- Session restoration refreshes Room from a valid Supabase session, clears a stale cached session when Supabase definitively reports no session, and retains the cache when the remote/profile lookup fails so an offline launch does not incorrectly sign the passenger out.
- Driver accounts are created only through a local administrator command using the Supabase Admin API; the service-role key never enters Android. Drivers enter phone/password in the UI, while the auth adapter deterministically maps the normalized phone to a confirmed `@shuttlespot.internal` email alias. The real phone remains in `profiles.phone_e164`, and no SMS provider is required. Login reads role, active state, and the first-login password flag from `profiles`, with the non-secret flag cached in Room for process restoration.
- Driver trip mutations use security-definer RPCs that derive the driver from `auth.uid()`, validate assignment and lifecycle transitions, and return an immutable trip snapshot. Direct authenticated updates to `trips` are revoked. Driver trip caching, Realtime, and foreground location publishing remain deferred.

## Data model (initial)

| Table/entity | Important fields |
| --- | --- |
| `profiles` | `id`, `role` (`passenger`, `driver`, `admin`), name, contact metadata, active, must-change-password |
| `routes` | `id`, name, direction, service notice, active, timestamps |
| `stops` | `id`, `route_id`, name, sequence, nullable latitude/longitude until verified map data exists |
| `route_geometry_points` | `route_id`, sequence, latitude, longitude; ordered MapLibre route line |
| `route_timetable_entries` | `route_id`, sequence, period, departure time, description |
| `vehicles` | `id`, plate number, display label, capacity, active |
| `trips` | `id`, route, vehicle, driver, `status`, started/ended timestamps, current-stop sequence |
| `vehicle_locations` | trip, latitude, longitude, accuracy, captured timestamp |
| `boarding_intents` | passenger, trip, stop, status, idempotency key, timestamps |
| `announcements` | author, route nullable, title, body, published timestamp |
| `notification_subscriptions` | profile, route, FCM token, preferences |
| Room outbox | operation type, payload version, idempotency key, retry count, last error, permanent-failure flag |

Use UUIDs. Model route assignments through `trips`; do not put mutable live-trip fields on `routes` or `vehicles`.

## Realtime and sync

1. Driver starts a trip and begins foreground location updates.
2. Each validated update writes to `vehicle_locations` and updates the trip's current state.
3. Passenger clients subscribe only to their active/selected route's trip changes and location feed.
4. The app writes a Room cache after successful reads/realtime events.
5. Offline actions go to the Room outbox. WorkManager retries when connected, and the server treats each idempotency key once.

Live location is the best effort, not a guarantee. UI must expose stale/offline states instead of pretending data is live.

## Security and privacy

- Enforce role- and row-level access in Supabase RLS, not only in the UI.
- Passengers can read only active route/trip data and manage only their own profile/intents.
- Drivers can write location and trip changes only for their assigned active trip.
- Admin writes are separately authorized.
- Keep Supabase URL/anon key in local untracked configuration; never place service-role keys in the Android app.
- Limit location collection to an active driver trip and explain it in-app. Do not store location more precisely or longer than the demo requires.

## Open implementation choices

- Choose whether ETA is calculated client-side from simplified route progress for the demo, or by an Edge Function. Start with a transparent heuristic and document it.
- Confirm UMN route/stops and access approval; otherwise ship explicit demo data.
- Decide how operations staff create trips for provisioned drivers; the current MVP uses explicit `trips.driver_id` assignment and intentionally has no Android admin surface.
