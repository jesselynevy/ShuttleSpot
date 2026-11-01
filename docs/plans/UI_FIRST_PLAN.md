# UI-first implementation plan

## Goal

Build the complete student-facing UI with deterministic in-memory data before connecting Supabase, Room, FCM, live maps, or device location. Preserve the ShuttleSpotVer1 design system exactly through BEG's theme, assets, and shared components.

## Design rule

`WaveBackground` is the single owner of route backgrounds. The root selects a `WaveStyle` from the current navigation destination and the feature screen renders foreground content only.

| Route area | Wave style | Screen responsibility |
| --- | --- | --- |
| Welcome | `Splash` | Title, bus image, entry action, access note, language label |
| Role selection and sign-in | `Auth` | Cards, fields, and actions |
| Main dashboard and tracker | `Main` | Content cards and sheets |
| Profile | `Profile` | Profile content |
| Full-screen sheets / neutral states | `None` | Surface/content only |

`WaveBackground` selects the corresponding light/dark asset. Screens must not render a second page-background image. Feature UI uses `MaterialTheme.colorScheme`, `LocalExtendedColors`, Material typography, and Material shapes; raw palette colors and hex values stay inside the theme only.

## Implementation order

1. **Foundation:** Hilt application/activity, Navigation Compose, root `ShuttleSpotApp`, `WaveBackground`, theme, fonts, assets, and string resources.
2. **Welcome and role selection:** migrate V1 compositions and route callbacks. Passenger selection is the only enabled continuation until driver UI begins.
3. **Student access UI:** email/password screen, inline domain validation, forgotten-password link placeholder, and loading/error presentation using local fake state.
4. **Permission education:** separate notification and location explanation screens. Location denial and skip both lead to manual route selection.
5. **Route setup:** route list, pickup/destination selection, stop-order validation, review, success screen, and saved primary selection in memory.
6. **Passenger shell:** Home, Routes, Notifications, and Profile tabs using V1 cards, badges, and progress components.
7. **Tracker demo:** simulated route line/stops/bus progress, stale-data label, and one-way boarding-intent UI. Keep MapLibre deferred.
8. **Driver scaffold:** phone-sign-in UI and no-assignment/assigned-trip visual states only after the student journey is usable.

## Reuse from ShuttleSpotVer1

Migrate selectively and change the package to `org.umn.shuttlespot`:

- Theme tokens, fonts, shapes, assets, `WaveBackground`, buttons, onboarding progress, feedback states, search, status badges, and route-progress UI.
- Welcome, role-selection, student onboarding, route-selection, success, Home, and tab layouts.
- Lightweight models and in-memory route/trip/announcement seed data.

Do not migrate the old database, Supabase configuration, location publisher, realtime adapter, WorkManager outbox, or full navigation graph during this phase.

## Completion checks

- Welcome and role-selection match V1 in both light and dark themes without duplicate background assets.
- A passenger can navigate from Welcome through all onboarding screens to Home using only demo data.
- Every destination has an intentional Back behavior, loading/empty state where applicable, and a narrow-device layout check.
- No feature composable uses raw colors, hardcoded typography, or direct backend/location calls.
