# AGENT.md — AI Coding Agent Context File

> **Read this file first** before making any changes to the codebase.

## Project Overview

**VibeChef** is an Android app (Kotlin + Jetpack Compose) that generates creative recipes using Google Gemini AI based on user-provided ingredients, a "vibe" (mood), and dietary restrictions. Users can authenticate, save recipes to Firebase Firestore, and browse their history.

## Quick Reference

| Item | Value |
|---|---|
| Language | Kotlin |
| UI Framework | Jetpack Compose + Material 3 |
| Min SDK | 28 (Android 9) |
| Target SDK | 36 |
| AI Backend | Google Gemini 2.5 Flash (`com.google.ai.client.generativeai`) |
| Auth | Firebase Auth (email/password) |
| Database | Firebase Firestore (per-user recipe collections) |
| Build System | Gradle Kotlin DSL with version catalog |
| Package | `fr.unica.fetheddine.lahjaily.vibechef` |
| App ID | `fr.unica.fetheddine.lahjaily.vibechef` |

## Repository Structure

```
td3-2025-lahjaily-fetheddine/          ← Git root
├── AGENT.md                           ← YOU ARE HERE
├── README.md                          ← Project readme
├── VibeChef/                          ← Android project root
│   ├── app/
│   │   ├── build.gradle.kts           ← App-level build config
│   │   ├── google-services.json       ← Firebase config (DO NOT commit secrets)
│   │   └── src/main/
│   │       ├── AndroidManifest.xml
│   │       ├── java/fr/unica/fetheddine/lahjaily/vibechef/
│   │       │   ├── MainActivity.kt
│   │       │   ├── data/              ← Repository layer
│   │       │   │   ├── AuthRepository.kt
│   │       │   │   ├── FirestoreRepository.kt
│   │       │   │   ├── GeminiRepository.kt
│   │       │   │   └── model/
│   │       │   │       └── Recipe.kt
│   │       │   └── ui/               ← Presentation layer
│   │       │       ├── AuthScreen.kt
│   │       │       ├── VibeChefScreen.kt      ← Main screen (445 lines)
│   │       │       ├── HistoryScreen.kt
│   │       │       ├── RecipeDetailScreen.kt
│   │       │       ├── components/
│   │       │       │   └── MarkdownText.kt
│   │       │       ├── navigation/
│   │       │       │   └── AppNavigation.kt
│   │       │       ├── theme/
│   │       │       │   ├── Color.kt
│   │       │       │   ├── Theme.kt
│   │       │       │   └── Type.kt
│   │       │       └── viewmodel/
│   │       │           ├── LoginViewModel.kt
│   │       │           └── MainViewModel.kt
│   │       └── res/
│   │           ├── drawable/          ← logo.png, header_image.png, icons
│   │           ├── values/
│   │           │   ├── strings.xml    ← All UI strings (French)
│   │           │   ├── colors.xml
│   │           │   └── themes.xml
│   │           └── xml/               ← Backup rules
│   ├── build.gradle.kts               ← Project-level build config
│   ├── settings.gradle.kts
│   └── gradle/
│       └── libs.versions.toml         ← Version catalog
```

## Architecture

Simple MVVM without DI framework:

```
UI (Compose Screens)
  ↕ StateFlow
ViewModels (MainViewModel, LoginViewModel)
  ↕ suspend functions / Flow
Repositories (GeminiRepository, FirestoreRepository, AuthRepository)
  ↕
External Services (Gemini API, Firebase Auth, Firestore)
```

- **No dependency injection** — repositories are instantiated directly in ViewModels via default constructor parameters.
- **No Room/local DB** — all persistence is Firebase Firestore only.
- **No Hilt/Dagger/Koin**.

## Navigation Flow

```
Login ──(auth success)──► Home ──► History ──► RecipeDetail
  ▲                         │
  └───(sign out)────────────┘
```

Routes defined in `AppNavigation.kt` via sealed class `Screen`:
- `login` → `AuthScreen`
- `home` → `VibeChefScreen`
- `history` → `HistoryScreen`
- `detail` → `RecipeDetailScreen`

## Key API / Environment

- **Gemini API Key**: Read from `GEMINI_KEY` environment variable at build time → `BuildConfig.API_KEY`
- **Firebase**: Configured via `google-services.json` (already in repo)

## Important Notes for Agents

1. **Package name contains collaborator names**: `fr.unica.fetheddine.lahjaily.vibechef` — this is a **pending refactor** (see TODO.md). Renaming the package is a high-impact change affecting AndroidManifest, build.gradle.kts, google-services.json, all Kotlin files, and Firebase project config.

2. **All UI strings are in French** (strings.xml). No English localization exists.

3. **The theme is generic** — uses default Material 3 purple/pink colors with dynamic color on Android 12+. No custom branding.

4. **VibeChefScreen.kt is the largest file** (445 lines) — it handles ingredient input, voice dictation, camera capture, vibe/filter selection, recipe generation, and recipe display all in one composable. Prime candidate for decomposition.

5. **MarkdownText.kt** is a minimal custom Markdown renderer (handles `#`, `###`, `**bold**` only). Does not support lists, links, or other Markdown features properly.

6. **No offline support** — requires network for both Gemini API and Firestore.

7. **No unit tests or UI tests** exist.

## See Also

- `docs/ARCHITECTURE.md` — Detailed architecture & data flow
- `docs/UI_STATE.md` — Current UI/UX state and known issues
- `docs/TODO.md` — Improvement backlog and enhancement ideas
