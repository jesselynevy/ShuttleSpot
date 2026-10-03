# Roadmap

Status key: `[ ]` not started, `[-]` in progress, `[x]` completed.

## 0. Proposal and context — complete

- [x] Convert the ShuttleSpot prototype report into product, architecture, design, and proposal context.
- [x] Curate the final IF450 PDF into `docs/source-of-truth.md` without adding its personal data to the repository.
- [x] Create the proposal deck and its passenger/driver mockups outside the repository; keep the deck out of Git because it contains personal information.

## 1. Android foundation

- [x] Initialize a Kotlin/Compose Android project and package structure.
- [-] Configure Material theme, navigation, Hilt, test baseline, and local untracked configuration template. The Material theme, single-activity Navigation Compose host, and baseline tests exist; Hilt and a local configuration template do not.
- [-] Create all 12 screen shells with fake state and navigation. All 12 destinations exist in the navigation graph, but most are still generic placeholders and only the live-trip screen has a map demo.
- [x] Restore a clean single-activity navigation host and local JDK 17 build configuration.

## 2. Data and authentication

- [ ] Create Supabase schema, RLS policies, and seed/demo data.
- [ ] Implement role-aware demo authentication and profile persistence.
- [ ] Add Room entities/DAOs for routes, stops, trips, announcements, and outbox.

## 3. Passenger vertical slice

- [ ] Route/stop selection and saved primary route.
- [ ] Local-first home with bus status, last-updated state, and announcement feed.
- [ ] Live trip map and passenger boarding intent.

## 4. Driver location and operations

- [ ] Assigned-trip dashboard and start/finish controls.
- [ ] Foreground location service during active trip only.
- [ ] Driver stop progression and route announcement creation.

## 5. Offline-first and notifications

- [ ] Implement outbox, idempotency, WorkManager sync, and offline UX.
- [ ] Add FCM token registration and notification deep links.
- [ ] Test offline actions and reconnect reconciliation.

## 6. Exam readiness

- [ ] Run device/emulator QA for all core screens and permission states.
- [ ] Prepare demo accounts/data and a short presentation demo script.
- [ ] Verify the final implementation against every acceptance criterion in `docs/product.md`.
