# Design

## Design principles

- Prioritize the next useful action: `where is my bus?`, `when should I leave?`, and `what changed?`.
- Show time-sensitive data with a clear status and `last updated` time.
- Use maps as supporting context, not the only way to understand a trip. Always provide a readable route/stop list.
- Keep driver controls large, low-distraction, and confirmation-based; do not require complex typing during a trip.
- Provide loading, empty, permission-denied, offline, and error states for every data-dependent screen.
- The first actionable onboarding button must be visible without scrolling on common phone sizes.
- Notifications are personal/activity-based; announcements are general operational information. Make their icon, title, destination, and content visibly different.

## Navigation

- Passenger onboarding: Welcome, role selection, student sign-in, notification permission, location permission, route setup/map review, information review, registration success.
- Passenger app: Home, Routes, Announcements, Notifications, Profile. The Route tab must show the saved active route and provide `Pilih Rute` to replace it.
- Driver: Home/Trip, Route, Announcements, Profile.
- Admin is a later role-gated dashboard, not part of the first passenger vertical slice.

## Required screen inventory (12+)

These screens meet the lecturer's minimum of 10 mockups and give the implementation an inspectable core flow.

1. Splash/welcome and role selection
2. Sign-in
3. Notification and passenger location-permission explanations
4. Route/origin/destination/time selection and map review
5. Passenger home — no active bus
6. Passenger home — active bus/ETA
7. Live trip map and stop progress
8. Boarding-intent confirmation/status
9. Student-information review and registration-success confirmation
10. Announcements feed and detail
11. Notifications inbox/preferences
12. Route tab with active-route replacement

The approved Flow ShuttleSpot MVP also requires the active/arrived/missed variations of Home and Tracker for capture. Driver screens remain optional after passenger P0.

Optional after MVP: route management, fleet dashboard, admin announcement composer, chat/help, language setting.

## Component rules

- Use a 4dp spacing grid. Start phone layouts at 32dp horizontal margins and 16dp content gutters, then adapt at smaller widths.
- Use Material 3 tokens; define semantic colors for `active`, `delayed`, `inactive`, `success`, and `warning` rather than scattering raw colors.
- Use one reusable `TripStatusCard`, `RouteChip`, `AnnouncementCard`, `OfflineBanner`, and `LastUpdatedLabel`.
- Use accessible labels for map controls and icon-only buttons. Aim for 48dp touch targets.
- Never express state through color alone; pair it with text and an icon.
- Confirm consequential actions: start trip, finish trip, cancel boarding intent, and publish announcement.
- Keep the route-progress indicator prominent and readable; it was the clearest validated element in the source usability test.
- Do not provide free-form passenger-to-driver chat when a driver is in an active trip. Use a short predefined response only if this interaction becomes necessary.

## Content language

Initial UI copy is Bahasa Indonesia. Keep strings externalized so English can be added later. Use plain, operational language such as `Bus tiba sekitar 5 menit`, `Data terakhir diperbarui 10:42`, and `Tidak ada perjalanan aktif`.
