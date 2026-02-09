# UI/UX Current State & Known Issues

## Screens Overview

### 1. AuthScreen (Login / Sign Up)
- **Layout**: Centered column with logo, title, email field, password field, action button, toggle link
- **Features**: Form validation (email format, password >= 6 chars), password visibility toggle, friendly Firebase error messages
- **Missing**: "Forgot password" is shown but **not implemented** (just a label)
- **Issues**:
  - No "Remember me" functionality
  - No social login (Google Sign-In etc.)
  - After sign-up, no email verification flow
  - No loading overlay — just a small `CircularProgressIndicator` inside the button

### 2. VibeChefScreen (Home — Main Screen)
- **Layout**: Top app bar → ingredients text field → restriction chips → vibe chips → camera images → cook button → recipe output
- **Features**:
  - Text input for ingredients with mic (speech-to-text) and camera icons
  - 3 filter chips: Végétarien, Sans Gluten, Épicé
  - 3 vibe chips: Rapide, Gourmet, Fun
  - Camera preview gallery (captured images shown as thumbnails)
  - Loading animation (pulsing edit icon + fading text)
  - Recipe output in a Card with Markdown rendering
  - Save to history, copy to clipboard, share via Intent
  - Dark mode toggle in top bar
  - Sign out button in top bar
- **Issues**:
  - **Massive file (445 lines)** — all logic in one composable, no decomposition
  - Ingredients text field is single-line — can overflow with long inputs
  - No scrolling for the input section — on small screens, chips + input may push the recipe area off-screen
  - Share FAB duplicates the share button in the top bar
  - No confirmation or visual feedback after saving a recipe (just a snackbar)
  - Camera captures only thumbnail-resolution bitmaps (via `TakePicturePreview`)
  - No gallery picker — can only take new photos
  - Voice input replaces entire ingredient text instead of appending
  - No character/ingredient count limit
  - Recipe title extraction is fragile (`lines().firstOrNull { it.startsWith("# ") }`)

### 3. HistoryScreen
- **Layout**: Top bar with back arrow → LazyColumn of recipe cards
- **Features**: Real-time Firestore listener, recipe cards show title + date
- **Issues**:
  - No delete functionality for recipes
  - No search/filter for history
  - No pull-to-refresh
  - No pagination (loads all recipes at once)
  - Empty state is just plain text centered on screen — no illustration

### 4. RecipeDetailScreen
- **Layout**: Top bar with back arrow → scrollable column with title + rendered Markdown + date
- **Issues**:
  - No share/copy buttons (unlike the main screen)
  - No edit capability
  - No delete button
  - Cannot re-generate or modify the recipe

## Theme & Styling

### Current State
- **Colors**: Default Material 3 template (Purple40/80, PurpleGrey40/80, Pink40/80)
- **Dynamic color**: Enabled on Android 12+ (ignores custom colors on those devices)
- **Typography**: Only `bodyLarge` is customized; all others use M3 defaults
- **Dark mode**: Toggle exists but preference is **not persisted** (resets on app restart)
- **XML theme**: `Theme.Material.Light.NoActionBar` — only used for splash/startup

### Issues
- No food/cooking-themed branding — looks like a generic template app
- No custom color palette that matches a "chef" or "food" identity
- No splash screen
- No app icon customization (uses default Android icon template)

## Markdown Renderer (`MarkdownText.kt`)

### Supported
- `# Heading` (skipped — handled by title extraction)
- `### Subheading` (rendered as `titleMedium`)
- `**bold text**` (inline bold)
- Blank lines (rendered as 4dp spacers)
- Plain text lines

### NOT Supported
- `## H2` headings
- Bullet lists (`- item`)
- Numbered lists (`1. step`)
- Italic (`*text*`)
- Links (`[text](url)`)
- Code blocks
- Emojis may render depending on device font but are not explicitly handled

This is a significant limitation since the Gemini prompt asks for bullet lists and numbered steps, which are then rendered as plain text without proper formatting.

## Localization

- **French only** — all strings in `values/strings.xml`
- Some hardcoded French strings exist directly in Kotlin code (e.g., `"Historique"`, `"Recette sauvegardée dans l'historique !"`, `"Aucune recette sélectionnée"`)
- No English `values-en/strings.xml`

## Accessibility

- Content descriptions exist for most icons (via `stringResource`)
- Some content descriptions are hardcoded in French
- No `semantics` blocks for custom accessibility
- No focus management or keyboard navigation optimization
