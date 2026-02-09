# Architecture & Data Flow

## Layer Overview

### 1. UI Layer (Compose Screens)

| Screen | File | Purpose |
|---|---|---|
| `AuthScreen` | `ui/AuthScreen.kt` | Login / Sign-up form with email + password |
| `VibeChefScreen` | `ui/VibeChefScreen.kt` | Main screen: ingredients input, vibe/filter chips, recipe output |
| `HistoryScreen` | `ui/HistoryScreen.kt` | List of saved recipes from Firestore |
| `RecipeDetailScreen` | `ui/RecipeDetailScreen.kt` | Full recipe view from history |
| `MarkdownText` | `ui/components/MarkdownText.kt` | Minimal Markdown renderer (headings + bold only) |

### 2. ViewModel Layer

| ViewModel | File | Responsibilities |
|---|---|---|
| `LoginViewModel` | `ui/viewmodel/LoginViewModel.kt` | Auth state, form validation, sign-in/up/out |
| `MainViewModel` | `ui/viewmodel/MainViewModel.kt` | Recipe generation, save, history, theme toggle |

**State management**: Both ViewModels use `MutableStateFlow` internally, exposing read-only `StateFlow` to the UI.

#### MainViewModel States (`UiState` sealed interface)

```
Initial ──(user clicks "Cuisiner!")──► Loading ──(success)──► Success(recipe: String)
                                         │
                                         └──(error/timeout)──► Error(message: String)
```

#### LoginViewModel States (`AuthUiState` sealed interface)

```
Idle ──(sign in/up)──► Loading ──(success)──► Authenticated(user)
                          │
                          └──(failure)──► Error(message)
```

### 3. Repository Layer (Data)

| Repository | File | Backend | Methods |
|---|---|---|---|
| `AuthRepository` | `data/AuthRepository.kt` | Firebase Auth | `signIn()`, `signUp()`, `signOut()`, `currentUser` |
| `GeminiRepository` | `data/GeminiRepository.kt` | Gemini 2.5 Flash | `generateRecipe(ingredients, vibe, filters, images)` |
| `FirestoreRepository` | `data/FirestoreRepository.kt` | Cloud Firestore | `saveRecipe()`, `getRecipes()` (returns `Flow<List<Recipe>>`) |

### 4. Model Layer

| Model | File | Fields |
|---|---|---|
| `Recipe` | `data/model/Recipe.kt` | `id`, `userId`, `title`, `content`, `timestamp` |

## Firestore Data Structure

```
users/
  └── {userId}/
      └── recipes/
          └── {recipeId}/
              ├── id: String
              ├── userId: String
              ├── title: String
              ├── content: String (full Markdown)
              └── timestamp: Long
```

## Gemini Prompt Engineering

The prompt is built in `GeminiRepository.generateRecipe()`:
- Includes ingredients (text + optional camera image analysis)
- Applies vibe context (Rapide/Gourmet/Fun)
- Enforces dietary restrictions with explicit rules
- Requests structured Markdown output: `# Title`, `### Ingrédients`, `### Instructions`
- Temperature set to `0.9` for creative output
- 30-second timeout enforced in ViewModel

## Navigation

Handled by `AppNavigation.kt` using Jetpack Navigation Compose.

**Route definitions** (sealed class `Screen`):
- `Screen.Login` → `"login"`
- `Screen.Home` → `"home"`
- `Screen.History` → `"history"`
- `Screen.Detail` → `"detail"`

**Start destination**: `Home` if `currentUser != null`, else `Login`.

Selected recipe for detail view is passed via `MainViewModel.selectedRecipe` StateFlow (not via navigation arguments).

## Theme

- Uses Material 3 with optional dynamic colors (Android 12+)
- Dark mode toggle stored in `MainViewModel.isDarkMode` (not persisted across app restarts)
- Default colors: Purple/Pink palette from template
- Custom theme applied in `MainActivity` via `VibeChefTheme(darkTheme = isDark)`

## Build Configuration

- API key injected via `System.getenv("GEMINI_KEY")` → `BuildConfig.API_KEY`
- Firebase configured via `google-services.json` + Google Services Gradle plugin
- Compose compiler plugin: `kotlin-compose`
- Version catalog: `gradle/libs.versions.toml`
