# ShuttleSpot UI-First Implementation Plan

## Decision and scope

## Approved-flow addendum (2026-10-06)

The approved [Flow ShuttleSpot](https://docs.google.com/document/d/17YCPWTSPf85NLzikkpIhXh7TjIyRhv21k0eYmK0YhJ4/edit?usp=sharing) document is the authority for passenger-screen order and behavior. Its summary is maintained in `docs/source-of-truth.md`.

Accordingly, P0 must include student email **and password input**, notification permission before location permission, route origin/destination/time selection and map review, student-information review, registration success, active-route change from the Route tab, and arrived/missed-bus states. The inputs and permissions may be implemented as deterministic prototype interactions, but the UI must not claim that real SSO, FCM delivery, or live GPS publishing is already connected.

This is the revised implementation order after the team decision to **defer Supabase implementation**. The immediate goal is a convincing, runnable Android UI prototype for the passenger journey, powered by deterministic in-memory demo data. It must not claim that GPS tracking, online storage, Realtime, notifications, Room sync, or authentication are already production-ready.

Do not delete the existing Supabase migration, configuration seam, Room scaffolding, or repository interfaces. They are useful future seams. Do not add a Supabase client, call the configured project, run migrations, or place credentials in tracked files during this UI-first phase.

The proposal still needs to **state** an online-storage plan and show it in the flowchart. That is a proposal requirement, even though the backend is not implemented in this sprint. The proposal can say: “Supabase is planned as the online database for user preferences, trips, and boarding confirmations; the current prototype uses demo data.” It must also show at least one native feature (GPS/location) in the mockups and flowchart. :codex-file-citation{path="C:\Users\User\Downloads\IF570_Project_Proposal_Assignment_Rubric (3).pdf" purpose="source"}

## What already exists in this project

The project is already a good starting point for a UI-first prototype:

| Existing foundation | Use in this phase | Do not do yet |
| --- | --- | --- |
| `navigation/ShuttleSpotNavGraph.kt` | Keep its onboarding, passenger, and driver destinations as the navigation contract. Replace placeholder content progressively. | Do not change routes just to connect a backend. |
| `SessionRepository`, `RouteRepository`, `PassengerTripRepository`, and `DriverTripRepository` | Let ViewModels continue to depend on these interfaces only. | Do not make screens call Supabase, Room, location, or map APIs directly. |
| `InMemory…Repository` classes | Extend them with controlled demo scenarios and route/boarding state. They are the source of every screen's data for now. | Do not replace them with remote adapters. |
| Passenger ViewModels and `FoundationScreens.kt` | Turn the basic shells into the passenger mockup screens below. | Do not put business rules directly in composables. |
| `LiveTripDemoScreen.kt` and `OpenStreetMap.kt` | Use a stable fake bus position and route-progress presentation. | Do not present the demo map as live GPS. |
| Room, WorkManager, Supabase config/migration, and `LocationPublisher` seams | Leave them compiling and document them as later work. | Do not schedule sync work, request background location, or use real credentials. |

Existing worktree changes are preserved. This plan adds work in small vertical slices rather than replacing the shared architecture.

## Priority order

### P0 — passenger prototype required for the proposal and demo

Finish this first. It is enough to produce more than the required ten connected mockups without depending on driver work.

1. **Stabilize the app shell and demo scenarios.**
   - Keep the current email-domain gate for a passenger and registered-phone lookup for a driver as demo eligibility checks only.
   - Add a small, explicit `DemoScenario` or equivalent state in the in-memory trip repository so the passenger UI can display: no active bus, en route, bus at pickup, missed bus, riding, and completed trip.
   - Use fixed route, stop, vehicle, ETA, and last-updated values. Mark every simulated data source in developer-facing copy/tests, not in the user-facing final mockup unless needed to avoid a misleading “live” claim.
   - Preserve the Indonesian `TripStatus.label` values as the display source.

2. **Build passenger onboarding and route setup.**
   - Welcome: visible `Masuk` button.
   - Role selection: `Mahasiswa` and `Driver`; staff remains out of scope.
   - Passenger sign-in: accept only `name@student.umn.ac.id`; show a useful validation error and continue only after success.
   - Location explanation: explain that location is optional and used only to suggest nearby stops. Provide `Izinkan lokasi`, `Pilih halte manual`, and a permission-denied/manual fallback.
   - Stop picker: list nearest demo stops when location is available; otherwise list all stops. Let the passenger choose pickup and destination.
   - Route results/review: show route name, ordered stops, departure times, pickup, destination, and a selected state. Save the selection through `RouteRepository` before continuing.
   - Registration/route success: confirm the saved demo choice and navigate to Home.

3. **Build the passenger Home state machine.**
   - Add the passenger bottom navigation: `Beranda`, `Rute`, `Notifikasi`, `Profil`. It may use non-functional placeholder tabs initially, but the selected tab and route screen must be clear.
   - Home must render each P0 scenario from the repository:
     - no active bus: next schedule plus `Atur rute`;
     - en route: vehicle, ETA, route progress, stop count, and `Konfirmasi akan naik`;
     - at pickup: a prominent “bus sudah tiba” action state;
     - missed: clear warning and a next-trip option;
     - riding: destination progress;
     - completed: completed status and next-schedule state.
   - Keep announcements visually separate from personal notifications.
   - Use reusable status card, route-progress, and last-updated components rather than duplicating the layout in each state.

4. **Build boarding-intent and live-tracker UI using fake data.**
   - Boarding confirmation opens from Home and Live Tracker. It shows bus, selected pickup stop, ETA, and a confirmation dialog.
   - Submit once through `PassengerTripRepository`; keep duplicate prevention through the existing idempotency key behaviour. Show a confirmed state and a cancel affordance if it fits the design.
   - Live Tracker shows a static/simulated map, route polyline or route-progress alternative, bus marker, selected stop, ETA, and an honest `Data terakhir diperbarui` value.
   - Map interaction is supportive: all essential bus progress and status must remain readable without interpreting the map.

5. **Polish, verify, and capture passenger mockups.**
   - Add loading, empty, error, and permission-denied states where the UI reads data or asks for location.
   - Keep Bahasa Indonesia copy and externalize new strings where practical.
   - Test the full passenger path on an emulator using fake data, then capture the screens for the proposal deck.

### P1 — driver UI mockups, only if time permits

Do this only after P0 is complete and the ten required mockups are ready. It is a visual/demo slice, not real GPS tracking.

1. Driver sign-in with registered phone lookup; no OTP and no SMS permission.
2. Driver Home before starting: assigned vehicle, route, shift, route-stop list, and waiting-passenger count.
3. Active Trip: simulated map location, current/next stop, stop progress, and large low-distraction `Tiba di halte ini` / `Lanjut ke halte berikutnya` actions.
4. Finish-trip confirmation and completed-trip screen.
5. Keep `LocationPublisher` as its no-op seam. Show text such as `Lokasi demo diperbarui` only when it is clear in the prototype context; do not request continuous GPS or foreground-service permission yet.

If P1 does not fit the schedule, do not weaken P0. The passenger-only inventory below already supplies the required ten mockups.

## Passenger mockup inventory (P0)

Use one mockup per slide and write two short notes below each: **what the user does** and **where the action goes next**.

| # | Screen | User action and next step |
| --- | --- | --- |
| 1 | Welcome | Taps `Masuk` → role selection. |
| 2 | Role selection | Chooses `Mahasiswa` → passenger sign-in. |
| 3 | Student email gate | Enters valid UMN student email → location explanation; invalid email stays with an error. |
| 4 | Location / nearby-stop explanation | Grants location for suggested stops or chooses manual selection → stop picker. This is the visible GPS/location integration. |
| 5 | Pickup and destination picker | Chooses both stops → route recommendations. |
| 6 | Route recommendation | Reviews route, schedule, and ordered stops → route review. |
| 7 | Route review / success | Saves primary route → passenger Home. |
| 8 | Home: no active bus | Reads next schedule or goes to routes. |
| 9 | Home: bus en route | Reads vehicle, ETA, progress, and last update → boarding confirmation or Live Tracker. |
| 10 | Live Tracker | Reviews simulated bus position, route progress, ETA, and last update → boarding confirmation. |
| 11 | Boarding confirmation | Confirms intention to board once → confirmed Home state. |
| 12 | Home exception state | Use either `Bus sudah tiba` or `Bus terlewat` to show that the status changes are handled. |

Optional driver screens can be added after these twelve, but they must not replace the passenger core-flow slides.

## Concrete implementation slices

Work in this order so every change leaves the app runnable.

1. **Route-selection slice**
   - Replace `RouteSetupScreen`'s “first route” shortcut with route cards, stop pickers, destination validation, and a review state.
   - Evolve `RouteSelectionViewModel` into an immutable `UiState` plus sealed `Event` API. It should load route details from `RouteRepository` and save only after the selection is valid.
   - Add ViewModel tests for missing pickup/destination, same/backward stop order, and successful save.

2. **Passenger-home slice**
   - Convert `PassengerHomeViewModel` from a raw `StateFlow<ActiveTripSnapshot?>` to a screen `UiState` that combines route selection, trip scenario, boarding state, and display-ready last-updated text.
   - Replace the simple `PassengerHomeScreen` with reusable cards and the scenario-specific sections.
   - Add tests for no-bus, active, stale, at-pickup, missed, riding, and completed presentation states.

3. **Live-tracker and boarding slice**
   - Pass the selected route/trip through repository state, rather than hard-coding `bsd_umn` in a screen.
   - Make the Live Tracker consume the ViewModel state and render the existing map component as a simulated map.
   - Implement a dedicated boarding-confirmation screen/ViewModel, not a navigation placeholder. Its only write is `submitBoardingIntent` on the in-memory repository.
   - Add a test proving repeated confirmation with the same idempotency key produces one confirmed intent.

4. **Navigation and UI-quality slice**
   - Keep `RequireRole` as the single guard for passenger and driver destinations.
   - Replace each P0 placeholder in `ShuttleSpotNavGraph.kt` only when its screen is ready. Do not make feature screens navigate across roles directly.
   - Add Compose/navigation tests for role protection and manually inspect narrow phone layouts, loading, empty, error, and location-denied states.

5. **Optional driver slice**
   - Only after the above is accepted, turn `DriverHomeScreen` and `DriverActiveTripScreen` into the four P1 mockups with the current `DriverTripViewModel` and in-memory repository.
   - Do not implement real GPS, foreground service, Supabase writes, or Realtime in this slice.

## Data and native-feature wording for the proposal

Use this implementation status consistently in the deck and when presenting:

- **GPS/location:** “The passenger may grant location permission to receive nearby-stop suggestions. The prototype uses a simulated location/trip state; active driver GPS publishing is a planned implementation.”
- **Online storage:** “Supabase is planned as the online database for profiles, route preferences, trips, vehicle locations, and boarding confirmations. This UI prototype currently uses deterministic in-memory demo data while the remote adapter is deferred.”
- **Offline-first:** Mention Room/outbox only as a future extra-credit path, not a delivered feature, until it has been implemented and tested.

In the flowchart, retain a database cylinder labelled `Planned Supabase` and use a dashed `planned integration` connector. Do not draw it as an already-working data path. The native GPS/location decision belongs between passenger sign-in and stop selection, with a `permission granted?` decision and a manual-stop fallback.

## Definition of done for this phase

The phase is complete when all of the following are true:

- A passenger can complete the UI flow from welcome to a saved primary route using only fake data.
- Passenger Home renders every promised bus-status state and does not imply that simulated data is live.
- The fake live tracker shows bus progress, ETA, and a last-updated timestamp.
- Boarding confirmation cannot create duplicate intent state for the same idempotency key.
- At least the twelve P0 screens above are ready to capture; each has a matching flowchart step and explanation.
- The proposal visibly names GPS/location and the planned Supabase database, while accurately distinguishing plans from current implementation. :codex-file-citation{path="C:\Users\User\Downloads\IF570_Project_Proposal_Assignment_Rubric (3).pdf" purpose="source"}
- Relevant unit tests and `testDebugUnitTest` pass; the passenger path is checked manually in an emulator.

## Debugging and integration checklist

Use this section when a passenger screen fails to load, displays the wrong route, or navigation does not complete. Debug one layer at a time; do not work around a state or repository issue inside a composable.

1. **Confirm the build is healthy.**
   - Run `./gradlew.bat :app:testDebugUnitTest` from the project root.
   - Run `./gradlew.bat :app:assembleDebug` before installing on an emulator.
   - Resolve compilation errors before diagnosing UI behavior. If Gradle is already running elsewhere, wait for it to finish rather than running concurrent builds against the shared worktree.

2. **Check the in-memory demo inputs.**
   - Inspect `InMemoryRouteRepository` for the intended route IDs, ordered stops, and timetable entries.
   - Inspect `InMemoryPassengerTripRepository` for the active-trip scenario, vehicle, ETA, location, progress, and last-updated value.
   - Keep every route ID and stop ID used by Home, Live Tracker, and Boarding aligned with the seeded repository data. Prefer saved selection/repository state over hard-coded IDs such as `bsd_umn` or `bsd_stop`.

3. **Check ViewModel state before UI layout.**
   - Route selection: verify missing pickup/destination, same-stop, and backwards stop-order validation before checking the picker layout.
   - Home: verify that each `TripStatus` maps to its intended `PassengerHomeUiState`, including no-active-bus, en-route, at-pickup, missed, riding, and completed states.
   - Boarding: verify that the same trip and pickup stop reuse one in-memory intent and that cancellation changes the intent status without creating a duplicate.
   - Tracker: verify the ViewModel observes the selected route/trip and shows a stable simulated timestamp. It must not claim a real GPS feed.

4. **Check navigation and access.**
   - Start at Welcome and exercise `Mahasiswa` sign-in, location explanation/manual fallback, route save, Home, Live Tracker, and Boarding confirmation.
   - Confirm an invalid student email does not continue.
   - Confirm passenger destinations remain protected by `RequireRole` and no screen navigates directly across roles.
   - Confirm Back returns to the expected previous screen without losing a valid saved selection.

5. **Check user-visible states on an emulator.**
   - Review loading, empty/no-bus, error, offline/stale, and location-denied states on a narrow phone layout.
   - Check button touch targets, scroll behavior, Indonesian copy, and the visibility of the primary action without accidental clipping.
   - Verify that status information remains understandable without the map and that `Data terakhir diperbarui` is visible for simulated trip data.

6. **Capture evidence before marking a slice complete.**
   - Keep the focused unit-test result and a manual emulator walkthrough result.
   - Capture the matching P0 mockup screen only after the relevant route, state, and navigation checks pass.
   - Record any deferred behavior as planned work; do not label demo data as Supabase, Realtime, GPS publishing, Room sync, or FCM behavior.

## Explicitly deferred backlog

After the UI-first phase, implement one backend capability at a time behind the existing interfaces: Supabase session/profile adapter, remote route data, boarding-intent persistence, driver foreground GPS publishing, Realtime subscription, Room cache/outbox, WorkManager sync, then FCM. Each capability needs an offline/error test before the next one begins.
