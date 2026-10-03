# Source of truth

## Authority order

When sources disagree, use them in this order:

1. The user's current request and lecturer requirements.
2. `AGENTS.md` for repository working rules.
3. This file for ShuttleSpot's validated research, prototype intent, and usability findings.
4. `docs/product.md`, `docs/architecture.md`, `docs/design.md`, and `docs/roadmap.md` for the current implementation plan.
5. Suggestions in older prototypes or visual mockups only when they do not conflict with the items above.

The implementation plan may deliberately limit the original prototype's scope for the final-exam MVP. It must not claim an unbuilt prototype feature is implemented.

## Primary source

**Title:** *Laporan Karya Akhir Semester IF450 Interaksi Manusia Komputer - BBENG: ShuttleSpot* (2026)

**Local source file:** `C:\Users\User\Downloads\JesselynVaniaAngelie_00000113854_IF450_EEN_UAS (2).pdf`

The PDF is the authoritative source for existing ShuttleSpot research and prototype design. It is intentionally not copied into this repository because it contains team personal information and should not be pushed to GitHub. This curated record preserves the implementation-relevant facts without that information.

## Supplementary proposal artifact

**Current proposal deck:** `C:\Users\User\Downloads\ShuttleSpotProposal.pptx` (2026).

Use the deck to preserve the scope presented to the lecturer. It confirms the research-backed passenger and driver journeys and makes these proposal commitments explicit:

- The proposal visual shows UMN SSO for passengers and registered-phone OTP for drivers. The current MVP decision replaces passenger SSO with a student-email domain gate: accept only a valid address in the form `name@student.umn.ac.id`.
- Passengers select pickup and destination stops, inspect route stops and departure schedules, then track the bus and confirm boarding intent.
- Drivers view waiting-passenger demand, follow their route, update trip status, move to the next stop, and finish the trip.
- Operations-staff features, including fleet monitoring, schedule management, and central announcements, are good-to-have rather than part of the first passenger-and-driver MVP.

The deck also contains personal information, so it must remain outside the repository. Where the deck and the research report differ, preserve the research findings and treat the deck as the current presentation-level scope unless the user or lecturer specifies otherwise.

## Validated problem and users

- ShuttleSpot serves UMN campus-shuttle passengers, drivers, and operational administrators.
- Research identified unreliable real-time bus-position information, schedules that may not match actual operations, and manual information sharing as primary problems.
- Passengers need real-time position, ETA, current operating status, updated schedules, and a way to tell a driver they intend to board at a stop.
- Drivers need a practical trip schedule, stop-level demand/boarding-intent information, simple status updates, and a way to distribute operational announcements.
- Administrators need central announcements and an operational/fleet-monitoring view.

## Validated feature scope

| Role | Validated features |
| --- | --- |
| Passenger | Live location map, ETA, operational status, per-route schedule, boarding confirmation |
| Driver | Waiting-passenger count, trip schedule, journey-status update, route-related announcement |
| Administrator | Central announcement, operational dashboard, real-time fleet monitoring |

## Usability findings that guide implementation

- The prototype was tested with six respondents with different campus-bus usage frequencies. Its overall average usability score was **3.78 / 4**.
- The bus journey progress bar was the clearest tested element, with an average of **4.00 / 4**. Keep readable progress between stops alongside the map.
- Bus position, schedules, notifications, announcements, and confidence that the app can prevent missed buses each scored **3.83 / 4**. Keep these as prominent passenger-home content.
- First-open clarity was the lowest-scoring measured item at **3.50 / 4** because a continuation button was not immediately visible. The primary onboarding action must be visible without scrolling on common phone sizes.
- Route selection and finding announcements scored **3.67 / 4**. Route selection must have a clear selected state and announcement navigation must be obvious.
- Users found notifications and announcements too similar. **Notifications are personal/activity-based** (for example, a bus nearing the passenger's stop); **announcements are general operational information** (for example, delay or route change). Use different labels, icons, destinations, and content types.
- Free-form chat with a driver was considered low-value and potentially distracting. Do not offer it while the driver is driving; prefer quick, predefined passenger responses where needed. Admin support/chat is a later, safer option.
- Use polished, high-resolution visual assets. Avoid unclean background removal or visibly pixelated bus illustrations.

## Design-system facts from the prototype

- Follow a 4dp spacing grid in Compose.
- The original design used approximately 32dp side margins and 16dp gutters; adapt responsively rather than forcing a fixed width.
- Apply a consistent Material 3 theme, reusable controls, visual hierarchy, and bottom navigation where appropriate.
- The source prototype used SF Pro and Tabler icons. For Android implementation, use system-safe typography and Material Symbols unless licensing and bundling of another asset set are explicitly handled.

## Research limitations

- Earlier user interviews involved two students and one intern; the source itself says these findings are not fully representative.
- The prototype research supports product direction and UI decisions, not a guarantee of real operational performance.
- Real UMN routes, schedules, accounts, and operating data require explicit approval. Use clearly labelled demo data until then.
