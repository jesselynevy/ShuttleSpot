# Architecture

## Guiding decisions

Use a single Android app with role-gated navigation. Build a vertical slice for the passenger journey first, then the driver location publisher. The cloud is authoritative, while Room provides a usable local cache and an outbox for offline writes.

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
- `SessionRepository`, `RouteRepository`, `PassengerTripRepository`, and `DriverTripRepository` are the public seams used by ViewModels. Hilt binds deterministic in-memory adapters by default so UI work does not depend on Supabase credentials.
- `LocationPublisher` is an isolated seam; its current no-op adapter is intentionally replaced later by the driver foreground-service implementation.
- Room contains cache/outbox scaffolding for routes, stops, selections, trip snapshots, announcements, and retryable passenger mutations. WorkManager is available for the future network-constrained sync worker, but no worker is scheduled until a Supabase adapter is configured.
- Supabase schema is versioned under `supabase/migrations/`; URL and anonymous-key values stay in untracked `local.properties` and are validated through `SupabaseConfig`.

## Data model (initial)

| Table/entity | Important fields |
| --- | --- |
| `profiles` | `id`, `role` (`passenger`, `driver`, `admin`), name, contact metadata |
| `routes` | `id`, name, direction, active |
| `stops` | `id`, `route_id`, name, sequence, latitude, longitude |
| `vehicles` | `id`, plate_number, capacity, active |
| `trips` | `id`, route, vehicle, driver, `status`, started/ended timestamps, current-stop sequence |
| `vehicle_locations` | trip, latitude, longitude, accuracy, captured timestamp |
| `boarding_intents` | passenger, trip, stop, status, idempotency key, timestamps |
| `announcements` | author, route nullable, title, body, published timestamp |
| `notification_subscriptions` | profile, route, FCM token, preferences |
| Room outbox | operation type, payload, idempotency key, retry metadata |

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
- Passenger demo sign-in validates a syntactically valid email whose domain is exactly `student.umn.ac.id`, then assigns the passenger role. Driver demo access checks that a normalized phone number matches a pre-registered driver profile before assigning the driver role. Neither flow verifies ownership of the submitted contact detail. Isolate both behind a role-aware auth adapter so a real identity provider can replace the demo later.
