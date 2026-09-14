# Design

## Design principles

- Prioritize the next useful action: `where is my bus?`, `when should I leave?`, and `what changed?`.
- Show time-sensitive data with a clear status and `last updated` time.
- Use maps as supporting context, not the only way to understand a trip. Always provide a readable route/stop list.
- Keep driver controls large, low-distraction, and confirmation-based; do not require complex typing during a trip.
- Provide loading, empty, permission-denied, offline, and error states for every data-dependent screen.

## Navigation

- Passenger: Home, Routes, Announcements, Notifications, Profile.
- Driver: Home/Trip, Route, Announcements, Profile.
- Admin is a later role-gated dashboard, not part of the first passenger vertical slice.

## Required screen inventory (12)

These screens meet the lecturer's minimum of 10 mockups and give the implementation an inspectable core flow.

1. Splash/welcome and role selection
2. Sign-in
3. Passenger permission/nearby-stop explanation
4. Route and stop selection
5. Passenger home — no active bus
6. Passenger home — active bus/ETA
7. Live trip map and stop progress
8. Boarding-intent confirmation/status
9. Announcements feed and detail
10. Notifications inbox/preferences
11. Driver dashboard — before trip
12. Driver active trip — map, next stop, and finish-trip confirmation

Optional after MVP: route management, fleet dashboard, admin announcement composer, chat/help, language setting.

## Component rules

- Use Material 3 tokens; define semantic colors for `active`, `delayed`, `inactive`, `success`, and `warning` rather than scattering raw colors.
- Use one reusable `TripStatusCard`, `RouteChip`, `AnnouncementCard`, `OfflineBanner`, and `LastUpdatedLabel`.
- Use accessible labels for map controls and icon-only buttons. Aim for 48dp touch targets.
- Never express state through color alone; pair it with text and an icon.
- Confirm consequential actions: start trip, finish trip, cancel boarding intent, and publish announcement.

## Content language

Initial UI copy is Bahasa Indonesia. Keep strings externalized so English can be added later. Use plain, operational language such as `Bus tiba sekitar 5 menit`, `Data terakhir diperbarui 10:42`, and `Tidak ada perjalanan aktif`.
