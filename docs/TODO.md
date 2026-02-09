# TODO — Improvement Backlog

> Priority: 🔴 High | 🟡 Medium | 🟢 Low

---

## 🔴 HIGH PRIORITY

### 1. Remove "Aya Fetheddine" references everywhere
**Status**: Not started
**Impact**: Package name, build config, all Kotlin files, Firebase project

Current package: `fr.unica.fetheddine.lahjaily.vibechef`
Target package: TBD (e.g., `com.mokhtarlahjaily.vibechef` or `com.vibechef.app`)

**Files affected**:
- `app/build.gradle.kts` → `namespace` and `applicationId`
- `AndroidManifest.xml` → activity fully-qualified name
- All `.kt` files → `package` declarations and `import` statements
- Directory structure: `java/fr/unica/fetheddine/lahjaily/vibechef/` → new path
- `google-services.json` → must match new `applicationId` (Firebase Console update needed)
- `README.md` → group members section

**Approach**: Use Android Studio's "Refactor > Move" on the package, or do a full find-and-replace + directory rename.

### 2. Update README.md
**Status**: Not started
- Remove classroom assignment references
- Remove Aya Fetheddine from group members
- Rewrite as a personal project README
- Remove GitHub Classroom badge

### 3. Improve MarkdownText renderer
**Status**: Not started
**Why**: Gemini generates bullet lists (`-`) and numbered lists (`1.`) but the renderer ignores them — they show as plain text.

**Needed support**:
- `- item` → bulleted list
- `1. step` → numbered list
- `## H2` headings
- `*italic*` text
- Better spacing between sections

**Options**:
- Enhance custom `MarkdownText.kt` composable
- Use a library like `compose-markdown` or `Markwon`

---

## 🟡 MEDIUM PRIORITY

### 4. Decompose VibeChefScreen.kt (445 lines)
**Status**: Not started

Extract into smaller composables:
- `IngredientsInputSection` — text field + mic + camera
- `FiltersSection` — restriction chips
- `VibeSelector` — vibe chips
- `CapturedImagesGallery` — camera preview thumbnails
- `RecipeCard` — recipe display with actions
- `LoadingState` — animated loading indicator
- `ErrorState` — error display with retry

### 5. Custom food-themed branding
**Status**: Not started
- Design a warm color palette (oranges, greens, warm browns)
- Custom app icon with chef/cooking motif
- Splash screen
- Disable dynamic colors to enforce brand consistency
- Custom typography (consider a friendly font)

### 6. Add delete functionality
**Status**: Not started
- Swipe-to-delete on HistoryScreen
- Delete button on RecipeDetailScreen
- Confirmation dialog before delete
- Add `deleteRecipe()` to FirestoreRepository

### 7. Persist dark mode preference
**Status**: Not started
- Use DataStore or SharedPreferences
- Load preference in MainViewModel init
- Currently resets every app restart

### 8. Add share/copy to RecipeDetailScreen
**Status**: Not started
- RecipeDetailScreen has no share or copy buttons (unlike VibeChefScreen)
- Should have the same action buttons as the main recipe card

### 9. Improve camera integration
**Status**: Not started
- Current: `TakePicturePreview` returns low-res thumbnail Bitmap
- Better: Use `TakePicture` with a FileProvider URI for full-res images
- Add gallery picker (`PickVisualMedia`) to select existing photos
- Show image count badge

### 10. Voice input should append, not replace
**Status**: Not started
- Currently, speech-to-text replaces the entire ingredients field
- Should append to existing text with a comma separator

---

## 🟢 LOW PRIORITY

### 11. English localization
- Add `values-en/strings.xml`
- Move hardcoded French strings from Kotlin code to string resources
- Hardcoded French strings found in:
  - `HistoryScreen.kt`: "Historique", "Retour", "Aucune recette sauvegardée..."
  - `RecipeDetailScreen.kt`: "Détails", "Retour"
  - `VibeChefScreen.kt`: "Recette sauvegardée dans l'historique !"
  - `AppNavigation.kt`: "Aucune recette sélectionnée"

### 12. Add dependency injection (Hilt/Koin)
- Replace manual repository instantiation in ViewModels
- Enables easier testing and configuration

### 13. Add offline support
- Cache recipes locally (Room database)
- Show cached data when offline
- Sync when back online

### 14. Add unit tests
- ViewModel tests (mock repositories)
- Repository tests
- UI tests with Compose testing

### 15. Implement "Forgot password"
- Currently shown as a label on AuthScreen but does nothing
- Wire up `FirebaseAuth.sendPasswordResetEmail()`

### 16. Recipe favorites / bookmarking
- Separate favorites from history
- Star/unstar recipes
- Dedicated favorites tab

### 17. Search & filter in history
- Search bar in HistoryScreen
- Filter by date range
- Sort options (newest, oldest, alphabetical)

### 18. Pagination for history
- Currently loads all recipes at once
- Implement Firestore cursor-based pagination
- Better performance for users with many saved recipes

### 19. Recipe image generation
- Use Gemini or another API to generate an image for each recipe
- Display as a header in RecipeDetailScreen

### 20. Onboarding screen
- First-launch tutorial
- Explain vibes, filters, voice input, camera features
