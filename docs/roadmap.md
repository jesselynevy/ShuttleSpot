# Roadmap

Status key: `[ ]` not started, `[-]` in progress, `[x]` completed.

## 0. Proposal and context — complete

- [x] Convert the ShuttleSpot prototype report into product, architecture, design, and proposal context.
- [x] Curate the final IF450 PDF into `docs/source-of-truth.md` without adding its personal data to the repository.
- [ ] Create the presentation slides from `docs/proposal-slides.md` and add the 12 required mockups.

## 1. Android foundation

- [ ] Initialize a Kotlin/Compose Android project and package structure.
- [ ] Configure Material theme, navigation, Hilt, test baseline, and local untracked configuration template.
- [ ] Create all 12 screen shells with fake state and navigation.

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
