# ShuttleSpot: Three-Member Task Board

## Goal

Finish the UI-first passenger prototype with deterministic in-memory data, ready for emulator walkthrough and proposal mockup capture. This board divides the remaining work into three tickets that minimize edit conflicts.

Read `AGENTS.md`, `docs/source-of-truth.md`, and `docs/ui-first-implementation-plan.md` before starting a ticket. The UI-first phase does **not** include Supabase, Room sync, WorkManager, FCM, real driver GPS publishing, or production authentication.

## Work order

```text
Ticket 1: Onboarding and route setup
        ↓ provides saved RouteSelection
Ticket 2: Passenger Home states ──────┐
                                      ├── Ticket 3: Tracker, boarding, and final integration
Ticket 1: Saved route contract ───────┘
```

Tickets 1 and 2 may begin together. Ticket 3 starts its route-selection integration only after Ticket 1 exposes a stable saved selection contract.

## Shared-work rules

- One person owns a file at a time. Do not make “small fixes” in another ticket’s files without telling that owner.
- Do not run Gradle concurrently with another member in this shared worktree.
- Do not edit `RepositoryModule.kt`, `AppDestination.kt`, or `ShuttleSpotNavGraph.kt` unless the ticket explicitly owns that change.
- Do not replace an in-memory repository with Supabase/Room code.
- Use fixed demo values and Indonesian UI copy.
- Before handoff, run `git diff --check`, the ticket’s focused tests, and report changed files plus any remaining issue.

---

## Ticket 1 — Onboarding, location choice, and route setup

**Owner:** Member 1  
**Priority:** P0  
**Depends on:** None  
**Unblocks:** Tickets 2 and 3

### Objective

Make the path from Welcome through a saved primary route work with deterministic route data:

```text
Welcome → Role selection → Student email gate → Location explanation →
manual/map stop choice → route result/review → route saved → Home
```

### Current starting point

The project already has onboarding shells, `LocationAssistanceScreen`, `RouteSelectionScreen`, `RouteSelectionViewModel`, `RouteRepository`, and `InMemoryRouteRepository`. Improve or complete them rather than rewriting the architecture.

### Owned files

- `feature/auth/AuthViewModel.kt`
- `feature/auth/SessionViewModel.kt` only when needed for the student-email flow
- `feature/foundation/FoundationScreens.kt` only for Welcome, role selection, or sign-in UI
- `feature/passenger/location/LocationAssistanceScreen.kt`
- `feature/passenger/route/RouteSelectionScreen.kt`
- `feature/passenger/route/RouteSelectionViewModel.kt`
- `data/repository/RouteRepository.kt`
- `data/repository/InMemoryRouteRepository.kt`
- Route-selection and route-repository tests

### Do not edit

- `feature/passenger/home/`
- `feature/passenger/livetrip/`
- `feature/passenger/boarding/`
- `data/repository/PassengerTripRepository.kt`
- `data/repository/InMemoryPassengerTripRepository.kt`
- Shared navigation files, except after agreement with Member 3 during final wiring

### Required work

- Preserve the email-domain gate: accept only valid `name@student.umn.ac.id` addresses. Do not implement password login or real SSO.
- Keep only `Mahasiswa` and `Driver` in the role selector; Staff is outside this prototype.
- Show an optional location explanation with `Izinkan lokasi` and `Pilih halte manual`.
- On denied/skipped permission, continue to manual stop selection. Never require background location.
- Let the passenger choose a route, pickup stop, and destination stop.
- Reject missing selections, equal stops, and a destination that comes before the pickup stop.
- Show route name, ordered stops, timetable, pickup, destination, review, and success state.
- Save the choice through `RouteRepository.savePrimaryRoute` only after validation succeeds.
- Ensure route IDs and stop IDs used in test/demo data are stable and match the current in-memory timetable.
- Leave a clear repository contract for others: `observePrimarySelection()` must yield the saved route, pickup stop, and destination stop.

### Acceptance checks

- Invalid email remains on sign-in with a helpful error.
- Manual flow works when location is denied or skipped.
- A valid pickup and later destination save successfully.
- Invalid stop ordering cannot be saved.
- Reopening route selection shows or can restore the saved selection.
- Route/repository focused tests pass.

### Handoff to the team

Report these exact values:

- the route ID and stop IDs seeded for the default demo;
- the final `RouteSelection` contract;
- focused test command/result;
- any navigation callback that Member 3 must connect.

---

## Ticket 2 — Passenger Home, status scenarios, and information UI

**Owner:** Member 2  
**Priority:** P0  
**Depends on:** Ticket 1 only for final saved-route hookup  
**Unblocks:** Proposal mockup capture and Ticket 3 final validation

### Objective

Turn Passenger Home into the main decision screen, matching the visual direction: blue header, route summary, readable trip card, progress, next action, announcements preview, and passenger bottom navigation.

### Owned files

- `feature/passenger/home/PassengerHomeScreen.kt`
- `feature/passenger/home/PassengerHomeViewModel.kt`
- `core/ui/components/BusStatusCard.kt`
- `core/ui/components/BusStatusRow.kt`
- `core/ui/components/BusRouteProgress.kt`
- `core/ui/components/StatusBadge.kt`
- `core/ui/components/ScreenFeedback.kt`
- `core/ui/components/LastUpdatedLabel.kt` if created
- `core/ui/components/AnnouncementCard.kt` if created
- `feature/passenger/home/` tests

### Do not edit

- Route-selection/location/auth files owned by Member 1.
- Tracker/boarding screens and passenger-trip repository owned by Member 3.
- `ShuttleSpotNavGraph.kt`; provide required callbacks to Member 3 instead.

### Required work

- Render these passenger states through a screen-specific `PassengerHomeUiState`:
  - no active bus;
  - en route;
  - bus at pickup;
  - missed bus;
  - riding;
  - completed trip;
  - loading, error, and stale/offline display.
- Use Indonesian `TripStatus.label` values as the display source where applicable.
- For no active bus, show a next schedule and `Atur rute` action.
- For an active bus, show vehicle, ETA, next/pickup stop, progress, a last-updated value, and actions for boarding/tracker when appropriate.
- Keep progress, ETA, and status readable without looking at a map.
- Keep general announcements separate from personal notifications. A static in-memory announcement preview is sufficient for this ticket; do not build FCM.
- Keep the four visible passenger tabs: `Beranda`, `Rute`, `Notifikasi`, `Profil`. An announcements shortcut may be shown separately from the personal notification tab.
- Receive navigation through callbacks such as `onRoutes`, `onLiveTrip`, `onBoardingIntent`, `onAnnouncements`, and `onNotifications`. The Home screen must not call `NavController` directly.
- When Ticket 1 is ready, update the ViewModel to derive the displayed route summary from `RouteRepository.observePrimarySelection()` rather than a hard-coded route label.

### Acceptance checks

- Every listed status renders sensible title, action, and feedback.
- Missed/completed states do not offer invalid boarding confirmation.
- The Home screen does not contain repository or navigation-controller calls.
- Narrow-phone layout scrolls and primary actions remain reachable.
- Focused ViewModel tests cover the six trip states plus at least loading/error/stale behavior.

### Handoff to the team

Report:

- callback names expected by navigation;
- any required shared component APIs;
- test result and screenshots/emulator states ready for capture.

---

## Ticket 3 — Live Tracker, boarding intent, navigation integration, and release check

**Owner:** Member 3  
**Priority:** P0  
**Depends on:** Ticket 1’s saved-route contract; Ticket 2’s Home callbacks  
**This is the integration owner.**

### Objective

Complete the fake-data Tracker and Boarding flow, then connect the finished passenger screens into one coherent, protected journey.

### Owned files

- `feature/passenger/livetrip/LiveTripDemoScreen.kt`
- `feature/passenger/livetrip/LiveTripViewModel.kt`
- `feature/passenger/boarding/BoardingIntentScreen.kt`
- `feature/passenger/boarding/BoardingIntentViewModel.kt`
- `data/repository/PassengerTripRepository.kt`
- `data/repository/InMemoryPassengerTripRepository.kt`
- `navigation/AppDestination.kt`
- `navigation/ShuttleSpotNavGraph.kt`
- Passenger-trip/boarding tests and relevant navigation tests

### Do not edit

- Route-selection files owned by Member 1.
- Passenger Home/component files owned by Member 2, except for a narrowly agreed compile fix.

### Required work

- Make Live Tracker consume ViewModel state, not a direct repository call or hard-coded screen values.
- Use MapLibre/the existing map component with fixed demo locations. Do not use Leaflet GeoSearch, a WebView, online geocoding, or real GPS publishing.
- Show simulated route/bus context, selected pickup/destination, ETA, progress, and `Data terakhir diperbarui`.
- Keep all essential state readable without the map.
- Make Boarding open from both Home and Tracker.
- Show bus, selected pickup stop, ETA, confirmation dialog, confirmed state, and cancellation affordance.
- Preserve idempotency: the same passenger/trip/pickup combination must never create duplicate intent state.
- Replace every hard-coded route/stop assumption in Home-facing passenger flow with data derived from the saved `RouteSelection` and active trip. Coordinate with Members 1 and 2; do not overwrite their files.
- Wire completed passenger screens in `ShuttleSpotNavGraph.kt` while preserving `RequireRole(UserRole.PASSENGER, …)` guards.
- Keep announcements/notifications as non-functional placeholders unless their completed screens are provided by another owner.
- Run the final P0 build and unit-test check only when other members are not running Gradle.

### Acceptance checks

- The saved route and pickup stop are the same values shown by Home, Tracker, and Boarding.
- Home → Tracker → Boarding → confirmed/cancelled → Back works without losing the saved selection.
- Tracker clearly represents simulated demo data and has last-updated information.
- Duplicate confirmation produces one intent only.
- Passenger cannot open protected passenger destinations without a passenger session.
- `git diff --check`, `:app:testDebugUnitTest`, and `:app:assembleDebug` pass.
- The full passenger path is manually walked through in an emulator.

### Handoff to the team

Publish a short release note containing:

- full manual test path;
- Gradle command results;
- any remaining placeholder screens;
- the twelve mockup states ready to capture;
- deferred work that must not be described as implemented.

---

## Shared final demo checklist

After Ticket 3 passes integration, review together:

- [ ] Welcome has a visible `Masuk` button.
- [ ] Passenger uses the email-domain demo gate, not real SSO.
- [ ] Location is optional and has manual fallback.
- [ ] Route, pickup, and destination are saved through the in-memory repository.
- [ ] Home supports no-bus, en-route, pickup, missed, riding, and completed states.
- [ ] Tracker is simulated and shows progress, ETA, and last update.
- [ ] Boarding confirmation is idempotent.
- [ ] Announcements and personal notifications are visibly distinct.
- [ ] P0 passenger screens are ready to capture for the twelve-screen proposal inventory.
- [ ] The proposal calls Supabase **planned**, not currently implemented.
- [ ] Driver work remains optional until passenger P0 is accepted.
