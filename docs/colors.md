# Colors and Typography

How to use the ShuttleSpot color and type system. Source files live in `app/src/main/java/com/beg/shuttlespot/core/ui/theme/`:

| File                | Purpose                                                                 |
| ------------------- | ----------------------------------------------------------------------- |
| `Color.kt`          | Raw palette values. Never reference these from feature UI.              |
| `Theme.kt`          | `ShuttleSpotTheme`, the Material 3 `ColorScheme` for light/dark.        |
| `ExtendedColors.kt` | App-specific tokens Material 3 has no slot for (`LocalExtendedColors`). |
| `Type.kt`           | `Inter` font family and `Typography`.                                   |

## The one rule

**Composables use theme tokens, not palette values.**

```kotlin
// Good
Box(Modifier.background(MaterialTheme.colorScheme.primary))
Box(Modifier.background(LocalExtendedColors.current.groupingColor))

// Bad: breaks dark mode and scatters raw colors
Box(Modifier.background(SpringSky))
Box(Modifier.background(Color(0xFF30AFF4)))
```

Tokens resolve to different values in light and dark. Raw values do not, so they look wrong in one of the two themes. `design.md` also requires semantic colors instead of raw ones scattered through the UI.

## Where to look for a color

1. **Does Material 3 have a slot for it?** (primary, background, surface, error, and so on) → `MaterialTheme.colorScheme.*`
2. **Is it one of the grey/neutral or background/grouping tokens below?** → `LocalExtendedColors.current.*`
3. **Is it a status color (success, pending, failed)?** → the status color mapping (see [Status colors](#status-colors)). Don't invent a new hex in the component.
4. **None of the above?** Add a token. See [Adding a new color](#adding-a-new-color).

## Palette (`Color.kt`)

### Brand

| Name        | Hex       | Role                                                             |
| ----------- | --------- | ---------------------------------------------------------------- |
| `SpringSky` | `#30AFF4` | Brand primary. Buttons, active route progress, links.            |
| `UMNBlue`   | `#015696` | Brand deep blue. Primary container (for example the bus marker). |

### Light theme

| Name              | Hex       | Mapped to                                 |
| ----------------- | --------- | ----------------------------------------- |
| `FullWhite`       | `#FFFFFF` | `groupingColor` (cards, grouped surfaces) |
| `BackgroundWhite` | `#F4F5F6` | `backgroundColor` (screen background)     |
| `ArabicGrey`      | `#868686` | `secondary1`                              |
| `SuperGrey`       | `#9A9A9A` | `secondary2`                              |
| `KawaiiSilver`    | `#DEDEDE` | `secondary3`                              |

### Dark theme

| Name           | Hex       | Mapped to                                 |
| -------------- | --------- | ----------------------------------------- |
| `DarkestColor` | `#0F1216` | `backgroundColor` (screen background)     |
| `LighterDark`  | `#181C21` | `groupingColor` (cards, grouped surfaces) |
| `DarkestGray`  | `#38465C` | `secondary1`                              |
| `MidGray`      | `#556680` | `secondary2`                              |
| `LightestGray` | `#90A3BF` | `secondary3`                              |

### Status colors

Same values in light and dark theme.

| Name                 | Hex       | Mapped to                                 |
| -------------------- | --------- | ----------------------------------------- |
| `SuccessContainer`   | `#C8F7D4` | `extended.successContainer`               |
| `OnSuccessContainer` | `#1B7A3D` | `extended.onSuccessContainer`             |
| `PendingContainer`   | `#FCE7A6` | `extended.pendingContainer`               |
| `OnPendingContainer` | `#8A5A00` | `extended.onPendingContainer`             |
| `ErrorContainer`     | `#FAD2D2` | Material 3 `errorContainer`               |
| `OnErrorContainer`   | `#B3261E` | Material 3 `onErrorContainer`             |
| `BlueContainer`      | `#DEF4FF` | `extended.blueContainer`                  |
| `OnBlueContainer`    | `#1484C0` | `extended.onBlueContainer`                |
| `PurpleContainer`    | `#EBE0FF` | `extended.purpleContainer`                |
| `OnPurpleContainer`  | `#7A40B8` | `extended.onPurpleContainer`              |
| `KawaiiSilver`       | `#DEDEDE` | `extended.greyContainer` (existing color) |
| `ArabicGrey`         | `#868686` | `extended.onGreyContainer` (existing)     |

### Unused template leftovers

`Purple40`, `PurpleGrey40`, `Pink40` are from the Android Studio template and are not part of the brand. Do not use them. They can be deleted.

## Material 3 color scheme (`Theme.kt`)

### Light (`LightColorScheme`)

Built from the same palette and designed to work with `extendedLight`: `background` and `surface` use the same values as `backgroundColor` and `groupingColor`, so Material components (dialogs, sheets, text fields) match custom ones.

| Slot                 | Value              | Use for                                                                                |
| -------------------- | ------------------ | -------------------------------------------------------------------------------------- |
| `primary`            | `SpringSky`        | Primary buttons, active/progress elements, icon tint                                   |
| `onPrimary`          | `DarkestColor`     | Text/icons on `primary` (dark on light blue, which has far better contrast than white) |
| `primaryContainer`   | `UMNBlue`          | Strong brand fills (bus marker)                                                        |
| `onPrimaryContainer` | `FullWhite`        | Text/icons on `primaryContainer`                                                       |
| `background`         | `BackgroundWhite`  | Screen background (same as `extendedLight.backgroundColor`)                            |
| `onBackground`       | `DarkestColor`     | Text on `background`                                                                   |
| `surface`            | `FullWhite`        | Cards, sheets, dialogs (same as `extendedLight.groupingColor`)                         |
| `onSurface`          | `DarkestColor`     | Text on `surface`                                                                      |
| `onSurfaceVariant`   | `ArabicGrey`       | Secondary text and icons from Material components                                      |
| `outline`            | `SuperGrey`        | Outlines, such as outlined buttons and text fields                                     |
| `outlineVariant`     | `KawaiiSilver`     | Subtle dividers                                                                        |
| `errorContainer`     | `ErrorContainer`   | Failed/error background                                                                |
| `onErrorContainer`   | `OnErrorContainer` | Text/icons on `errorContainer`                                                         |

### Dark (`DarkColorScheme`)

Built from the same palette and designed to work with `extendedDark`: `background` and `surface` use the same values as `backgroundColor` and `groupingColor`, so Material components (dialogs, sheets, text fields) match custom ones.

| Slot                 | Value              | Use for                                                                                                   |
| -------------------- | ------------------ | --------------------------------------------------------------------------------------------------------- |
| `primary`            | `SpringSky`        | Primary buttons, active/progress elements, icon tint                                                      |
| `onPrimary`          | `DarkestColor`     | Text/icons on `primary` (dark on light blue, which also avoids the low contrast white has on `SpringSky`) |
| `primaryContainer`   | `UMNBlue`          | Strong brand fills (bus marker)                                                                           |
| `onPrimaryContainer` | `FullWhite`        | Text/icons on `primaryContainer`                                                                          |
| `background`         | `DarkestColor`     | Screen background (same as `extendedDark.backgroundColor`)                                                |
| `onBackground`       | `FullWhite`        | Text on `background`                                                                                      |
| `surface`            | `LighterDark`      | Cards, sheets, dialogs (same as `extendedDark.groupingColor`)                                             |
| `onSurface`          | `FullWhite`        | Text on `surface`                                                                                         |
| `onSurfaceVariant`   | `LightestGray`     | Secondary text and icons from Material components                                                         |
| `outline`            | `MidGray`          | Outlines, such as outlined buttons and text fields                                                        |
| `outlineVariant`     | `DarkestGray`      | Subtle dividers                                                                                           |
| `errorContainer`     | `ErrorContainer`   | Failed/error background (same value as light)                                                             |
| `onErrorContainer`   | `OnErrorContainer` | Text/icons on `errorContainer` (same value as light)                                                      |

Slots not listed (`secondary`, `tertiary`, `error`, and so on) are still Material defaults in both themes. Do not use them yet.

`ShuttleSpotTheme` also sets `LocalContentColor` to `onBackground`, so any `Text` without an explicit color is `FullWhite` in dark mode and `DarkestColor` in light mode. `darkTheme` follows the system setting (`isSystemInDarkTheme()`).

### Using both together

Pair them like this so each theme stays consistent:

| Need                        | Token                                                                                           |
| --------------------------- | ----------------------------------------------------------------------------------------------- |
| Screen background           | `LocalExtendedColors.current.backgroundColor` (or `colorScheme.background`, same value in dark) |
| Card on the screen          | `LocalExtendedColors.current.groupingColor` (or `colorScheme.surface`, same value in dark)      |
| Main text                   | `colorScheme.onBackground` / `onSurface`                                                        |
| Helper text, inactive icons | `LocalExtendedColors.current.secondary2`                                                        |
| Borders, tracks             | `LocalExtendedColors.current.secondary3` (see the warning below)                                |
| Brand/active elements       | `colorScheme.primary` and `onPrimary`                                                           |
| Status badge colors         | `StatusBadge` (see [Status colors](#status-colors))                                             |

## Extended colors (`ExtendedColors.kt`)

Read them from anywhere under `ShuttleSpotTheme`:

```kotlin
val extended = LocalExtendedColors.current
Text("Terakhir diperbarui 10:42", color = extended.secondary2)
```

| Token                                     | Light                                     | Dark           | Use for                                                     |
| ----------------------------------------- | ----------------------------------------- | -------------- | ----------------------------------------------------------- |
| `backgroundColor`                         | `BackgroundWhite`                         | `DarkestColor` | Screen background                                           |
| `groupingColor`                           | `FullWhite`                               | `LighterDark`  | Cards and grouped content sitting on the background         |
| `secondary1`                              | `ArabicGrey`                              | `DarkestGray`  | See the naming warning below                                |
| `secondary2`                              | `SuperGrey`                               | `MidGray`      | Secondary/helper text, inactive icons, inactive step labels |
| `secondary3`                              | `KawaiiSilver`                            | `LightestGray` | Borders, dividers, inactive track                           |
| `successContainer` / `onSuccessContainer` | `SuccessContainer` / `OnSuccessContainer` | same           | Success badge background / text                             |
| `pendingContainer` / `onPendingContainer` | `PendingContainer` / `OnPendingContainer` | same           | Pending badge background / text                             |
| `blueContainer` / `onBlueContainer`       | `BlueContainer` / `OnBlueContainer`       | same           | Info/blue badge background / text                           |
| `purpleContainer` / `onPurpleContainer`   | `PurpleContainer` / `OnPurpleContainer`   | same           | Purple badge background / text                              |
| `greyContainer` / `onGreyContainer`       | `KawaiiSilver` / `ArabicGrey`             | same           | Grey (inactive) badge background / text                     |

### Layering

`backgroundColor` is the lowest layer and `groupingColor` sits on it. In light mode, white cards sit on a light-grey screen. In dark mode, the slightly lighter card sits on the near-black screen. Don't put `groupingColor` directly on `groupingColor`.

### Known issue: `secondary1-3` do not mean the same thing in both themes

The names are positional (dark to light in the dark theme, grey to silver in the light theme), but the *role* flips:

- Light: `secondary3` is the **most subtle** grey (good for borders).
- Dark: `secondary3` (`LightestGray`) is the **brightest** grey, so borders and track lines that use it (`BusStatusCard`, `BusRouteProgress`, `BusStatusRow`) will look harsh in dark mode.

This is the main open issue with dark mode. Decide one of these:

1. Swap the dark values so each token keeps the same *role* (`secondary3` = subtlest, `secondary2` = helper text) in both themes. This is the safest and does not require touching call sites.
2. Rename the tokens by role (`border`, `onSurfaceMuted`, ...).

Until then, check any use of `secondary1-3` in dark mode before assuming it works. `secondary3` is the one that looks wrong now that dark mode is live.

## Status colors

Status badges (`StatusBadge`) read their colors from the theme. Nothing is hardcoded in the component anymore.

| `LabelColor` | Container (background)                 | Content (text/border)                    | Comes from               |
| ------------ | -------------------------------------- | ---------------------------------------- | ------------------------ |
| `Success`    | `extended.successContainer` (#C8F7D4)  | `extended.onSuccessContainer` (#1B7A3D)  | `LocalExtendedColors`    |
| `Pending`    | `extended.pendingContainer` (#FCE7A6)  | `extended.onPendingContainer` (#8A5A00)  | `LocalExtendedColors`    |
| `Failed`     | `colorScheme.errorContainer` (#FAD2D2) | `colorScheme.onErrorContainer` (#B3261E) | Material 3 `ColorScheme` |
| `Blue`       | `extended.blueContainer` (#DEF4FF)     | `extended.onBlueContainer` (#1484C0)     | `LocalExtendedColors`    |
| `Purple`     | `extended.purpleContainer` (#EBE0FF)   | `extended.onPurpleContainer` (#7A40B8)   | `LocalExtendedColors`    |
| `Grey`       | `extended.greyContainer` (#DEDEDE)     | `extended.onGreyContainer` (#868686)     | `LocalExtendedColors`    |

`LabelStyle.Soft` uses container as the background and content as the text. `LabelStyle.Solid` swaps them.

Rules:

- Use `StatusBadge` rather than recreating these colors in a new component. Failed/error is the only one that lives in the Material 3 scheme, so use `colorScheme.errorContainer` / `onErrorContainer` for any other error surface.
- Never express a state through color alone. Pair it with text and an icon (`design.md`).
- **The dark theme uses the same values as light for every status color.** These are light pastels, so on a dark card they will look bright. That is intentional for now. If it looks too glaring, give `extendedDark` (and `DarkColorScheme` for error) darker container values. No call sites need to change.
- `design.md` calls for semantic `active`, `delayed`, `inactive`, `success`, `warning` colors. `success` and `pending` (warning) exist now. `active`/`delayed`/`inactive` are not separate tokens yet.

## Typography (`Type.kt`)

### Font

`Inter` (`FontFamily`) from `res/font/`:

| Weight     | File                      |
| ---------- | ------------------------- |
| `Normal`   | `inter_18pt_regular.ttf`  |
| `SemiBold` | `inter_18pt_semibold.ttf` |
| `Bold`     | `inter_24pt_bold.ttf`     |

### Customized styles

| Style        | Size / line height | Weight | Letter spacing |
| ------------ | ------------------ | ------ | -------------- |
| `bodyLarge`  | 16 / 24 sp         | Normal | 0.5 sp         |
| `titleLarge` | 22 / 28 sp         | Normal | 0 sp           |
| `labelSmall` | 11 / 16 sp         | Medium | 0.5 sp         |

Use them through the theme:

```kotlin
Text("Rute Kampus", style = MaterialTheme.typography.titleLarge)
```

Combine with a color token, never a raw color: `Text(..., style = MaterialTheme.typography.bodyLarge, color = LocalExtendedColors.current.secondary2)`.

### Known gaps

- **Only three styles use Inter.** Every other style (`headlineMedium`, `labelMedium`, `bodySmall`, and so on) is still Material's default and renders in the system font. The code already uses `headlineMedium`, `labelMedium` and `bodySmall`, so the app currently mixes Inter and the system font. Fix by defining the remaining styles in `Typography` or by applying `Inter` as the default for all of them.
- **`labelSmall` uses `FontWeight.Medium`, but `Inter` has no Medium file.** Compose falls back to the nearest weight, so it won't render as true Medium. Add an Inter Medium file or change the weight to `Normal` or `SemiBold`.
- Prefer `MaterialTheme.typography.*` over ad hoc `TextStyle(...)` and hardcoded `sp` values.

## Accessibility

- **`onPrimary` on `SpringSky`:** both themes use `DarkestColor` as `onPrimary`. White on `#30AFF4` is only about 2.4:1, below the WCAG AA minimum (4.5:1 for normal text), so don't put white text on `SpringSky`.
- Don't rely on color alone for state. Pair with text and an icon.
- Aim for 48dp touch targets (`design.md`).

## Adding a new color

1. Is it really new? Check the palette and the tables above first.
2. Add the raw value to `Color.kt`, grouped with similar ones, with a descriptive name.
3. Expose it through a token:
   - Material 3 has a matching slot → set it in **both** `LightColorScheme` and `DarkColorScheme`.
   - Otherwise add a field to `ExtendedColors` and set it in **both** `extendedLight` and `extendedDark`. Also add it to the `LocalExtendedColors` default instance (`Color.Unspecified`).
4. Use the token in the component, not the raw value.
5. Update the tables in this file.

Every token must have a light and a dark value. A token defined for only one theme is a bug.

## Quick checklist for a new screen or component

- [ ] No `Color(0x...)` and no palette names (`SpringSky`, `ArabicGrey`, ...) in feature UI
- [ ] Backgrounds use `backgroundColor` / `groupingColor`
- [ ] Brand-colored elements use `MaterialTheme.colorScheme.primary` or `primaryContainer` and their `on*` pair
- [ ] Text uses `MaterialTheme.typography.*`
- [ ] State is shown with text and an icon, not just color
- [ ] Looked at in both light and dark mode

## Known hardcoded colors to migrate

These bypass the theme and will look wrong in dark mode:

| File                   | Value                                          | Suggested token                                     |
| ---------------------- | ---------------------------------------------- | --------------------------------------------------- |
| `BusIconWithStatus.kt` | `#DEF4FF` circle background                    | `extended.blueContainer` |
| `BusStatusRow.kt`      | `#912A43` ("bus already passed" icon and text) | A semantic `warning`/`error` token                  |
