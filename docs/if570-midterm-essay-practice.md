# IF570 Midterm Essay Practice

This set covers Meetings 1-7 and uses the quiz's applied, scenario-based style. Write each answer in 3-6 sentences before comparing it with the model answer.

## 1. Android fundamentals

### Q1. App launch configuration

A project installs successfully, but tapping its icon does not open the intended screen. Which file should be checked first, what declaration must be verified, and why?

**Model answer:** Check `AndroidManifest.xml`. It declares application components to Android, including activities. Verify that the intended activity is registered and, if it is the entry point, has an intent filter containing `MAIN` and `LAUNCHER`. Without this information, Android cannot identify the activity to start from the launcher.

### Q2. Minimum SDK

An app has `minSdk = 26`, but a developer calls an API that only exists from API 31. Explain the risk and give two valid solutions.

**Model answer:** The app can run on devices with APIs 26-30, so calling an API 31 function directly can crash on those devices. The developer can check `Build.VERSION.SDK_INT` and provide a fallback for older devices. Alternatively, the team can raise `minSdk` to 31 if excluding older devices is acceptable. The choice affects the app's supported audience.

## 2. Compose layout

### Q3. Layout selection

Design a profile card with an avatar on the left, a name above an email in the center, and an action icon on the right. Which layout composables would you use, and why?

**Model answer:** Use a `Row` as the outer container because the main areas are horizontal. Use a `Column` in the center to stack name and email vertically. Avatar and action icon are sibling items in the Row. Giving the center Column `Modifier.weight(1f)` lets it occupy the remaining width while preserving space for the icon.

### Q4. Modifier order

Compare these modifier chains:

```kotlin
Modifier.background(Color.Gray).padding(16.dp)
Modifier.padding(16.dp).background(Color.Gray)
```

Explain the visual difference and why it happens.

**Model answer:** In the first chain, the gray background is drawn before padding, so the padded area is gray. In the second, padding is outside the background, so only the inner content area is gray. Modifiers form an ordered chain: each one receives the result of the previous one. Therefore, changing order can change drawing, size, borders, and click areas.

## 3. Material Design 3

### Q5. Semantic theming

A developer uses the same hardcoded green hex value in 30 buttons. Explain the problem and how a Material 3 `ColorScheme` improves the design.

**Model answer:** Repeated hardcoded values make brand changes and dark-mode support inconsistent and expensive. Define light and dark `ColorScheme` values and pass them into `MaterialTheme`. Buttons should use semantic roles such as `MaterialTheme.colorScheme.primary`, not literal colors. A theme change then propagates consistently across the app.

### Q6. Dark theme and dynamic color

What is the difference between dark theme and dynamic color? When is dynamic color available, and what should the app do when it is unavailable?

**Model answer:** Dark theme chooses a dark color scheme; dynamic color derives a scheme from the user's wallpaper or system colors. Android dynamic color is available from API 31 and is optional. The app can use `dynamicLightColorScheme` or `dynamicDarkColorScheme` on supported devices and use its own static light or dark scheme as a fallback. The light/dark choice may follow `isSystemInDarkTheme()` or a user preference.

## 4. State management

### Q7. Observable state and recomposition

Why does changing a normal Kotlin variable such as `var count = 0` not reliably update Compose UI, while `mutableStateOf(0)` does?

**Model answer:** A normal variable is not observed by Compose, so changing it does not notify the UI. `mutableStateOf` creates observable state. If a composable reads that state, changing it schedules recomposition of the dependent UI, which redraws it with the new value. In Compose, UI is a function of observable state.

### Q8. State ownership

A `SearchBar` holds its own query, but the parent screen needs the query to filter a list. Explain state hoisting and the direction of data flow after refactoring.

**Model answer:** Move the query state to the parent screen or ViewModel. Pass `query: String` down to `SearchBar` and pass an `onQueryChange: (String) -> Unit` callback to send typing events up. State flows down from the owner to the child; events flow up from the child to the owner. This unidirectional data flow makes the child reusable and creates one source of truth.

## 5. Navigation Compose

### Q9. Navigation roles

Explain the relationship between `NavController`, `NavHost`, `NavGraph`, and a route using routes `home`, `settings`, and `detail/{id}`.

**Model answer:** A route is a unique destination address such as `home` or `detail/{id}`. A NavGraph defines the destinations and their route-to-composable mapping. NavHost is the composable that displays the currently selected destination from that graph. NavController manages navigation and the back stack, for example when `navigate("detail/7")` is called.

### Q10. Passing detail data

A list opens a schedule detail screen for schedule ID 42. How should the route and argument be handled, and why is sending only the ID preferable to sending the whole object?

**Model answer:** Define a route such as `detail/{scheduleId}` and call `navController.navigate("detail/42")`. On the destination, read and validate `scheduleId` from the back-stack arguments. Passing an ID keeps routes small and avoids object serialization problems. The detail screen can use the ID to load the latest data through its ViewModel or repository.

## 6. MVVM

### Q11. Layer responsibilities

A composable reads Room, validates input, launches coroutines, and renders a list. Explain how MVVM should separate this work among View, ViewModel, Repository, and Room.

**Model answer:** The View should render UI state and forward user events only. The ViewModel owns UI state, validation, and decisions; it uses `viewModelScope` for asynchronous work. A Repository hides where data comes from and exposes a data contract. Room is the concrete local data source accessed behind the repository. This separation makes code easier to test, replace, and maintain.

### Q12. Read-only state

Why should a ViewModel keep `MutableStateFlow` private while exposing only `StateFlow` to a composable?

**Model answer:** The ViewModel should be the only layer allowed to mutate UI state, so it can apply validation and business rules. The UI observes a read-only `StateFlow` with `collectAsState()` or `collectAsStateWithLifecycle()`. Exposing `MutableStateFlow` lets the UI mutate state directly, bypassing the ViewModel. That breaks unidirectional data flow and makes state changes harder to reason about.

## 7. Room and SQLite

### Q13. Room building blocks

Explain the roles of `@Entity`, `@Dao`, and a class extending `RoomDatabase` in a notes app with a `notes` table.

**Model answer:** `@Entity` maps a Kotlin data class such as `NoteEntity` to the `notes` table and its columns. `@Dao` declares data operations, for example `@Query`, `@Insert`, and `@Delete`. A class extending `RoomDatabase` provides the database configuration and abstract access to the DAO, such as `noteDao()`. Room generates the concrete implementations during build.

### Q14. Snapshot versus live list

Compare these DAO functions and explain how the second one keeps a Compose list up to date:

```kotlin
suspend fun getAll(): List<NoteEntity>
fun observeAll(): Flow<List<NoteEntity>>
```

**Model answer:** The suspend function returns one snapshot at the time it is called, so it suits one-time reads. The Flow function is reactive: Room emits a new list when observed table data changes. A repository or ViewModel can expose it as `StateFlow`, and Compose can collect it as state. Inserts, updates, and deletes then cause state changes and recomposition without manually refreshing the list.

### Q15. Migration

Version 1 has `notes(id, title, content)`. Version 2 adds `createdAt`. Why must the database version increase, and why is a migration important?

**Model answer:** The schema changes, so the version must increase to tell Room that the old database structure no longer matches the current entity. A migration describes how to transform existing user databases, for example by adding `createdAt` with a valid default. Without an appropriate migration, the old database may fail to open or need destructive recreation. A tested migration preserves existing notes.

## Self-check rubric

For every answer, make sure you can:

- Name the correct API or concept.
- Explain how or why it works, not only define it.
- Connect it to lifecycle, UI behavior, maintainability, or persistence.
- Use the scenario in the question as evidence for your answer.

