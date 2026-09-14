# Android feature workflow

Use this procedure when implementing or changing an Android feature.

1. Read `AGENTS.md`, the relevant product/design sections, architecture, and roadmap item.
2. Inspect existing navigation, Compose components, ViewModels, repository interfaces, Room entities, and Supabase contracts before adding new ones.
3. Implement UI as state-driven Compose; keep IO, permissions, location, and sync outside composables.
4. Design every remote-data screen for loading, empty, error, and offline/stale data.
5. For mutations, use the repository/outbox path and idempotency keys; do not bypass local-first behaviour.
6. For location, request permission in context, publish only during an active driver trip, and test denied permission/end-trip cleanup.
7. Run the smallest relevant Gradle checks/tests and manually inspect the affected flow on an emulator.
8. Update product, architecture, or roadmap documentation when behaviour, contracts, or milestone state changes.
