# Project instructions

## Goal
Build **ShuttleSpot**, an Android app for UMN campus-shuttle passengers, drivers, and operations staff. It reduces uncertainty around bus location, arrival time, and service updates.

## Stack
- Kotlin, Jetpack Compose, Material 3, Navigation Compose
- Supabase (Auth, Postgres, Realtime, Storage if needed)
- Room + WorkManager for offline-first persistence and sync
- Google Maps Compose and Android location APIs
- Firebase Cloud Messaging for push notifications

## Important files
- Product requirements and flows: `docs/product.md`
- Research and prototype source of truth: `docs/source-of-truth.md`
- Architecture and data model: `docs/architecture.md`
- UI rules and required screens: `docs/design.md`
- Color, theme, and typography usage: `docs/colors.md`
- Navigation skeleton and how to extend it: `docs/skeleton-guide.md`
- Current milestones: `docs/roadmap.md`
- Proposal-slide outline: `docs/proposal-slides.md`

## Working rules
- Read `docs/source-of-truth.md` and the relevant product, architecture, design, and roadmap docs before changing behaviour.
- Make the smallest change that fully solves the task; do not build speculative features.
- Keep the passenger flow working before expanding driver or admin functionality.
- Use Compose state holders/ViewModels; UI composables must not call Supabase, Room, or location APIs directly.
- Treat location and authentication data as sensitive. Request runtime permissions only when the feature is used.
- Never commit secrets, Supabase keys, `google-services.json`, or edit tracked local configuration with credentials.
- Update `docs/architecture.md` for schema, sync, or dependency decisions; update `docs/product.md` for user-visible changes; update `docs/roadmap.md` when completing a milestone.

## Verification
- Run the relevant Gradle checks and focused unit tests.
- For UI changes, run the affected Compose screen/emulator flow and inspect loading, empty, error, and offline states.
- For location or sync changes, test denied permission and offline-to-online recovery.

## Definition of done
- The requested behaviour works for the intended role.
- Build/tests/lint appropriate to the change pass.
- No secrets or unrelated formatting changes are introduced.
- Relevant documentation and roadmap status are current.
