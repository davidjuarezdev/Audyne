# VaultDraft

An open-source, non-subscription Android alternative to **Quick Draft for Obsidian**.
Capture text, voice notes (speech-to-text) and scanned images (offline OCR) straight into your Obsidian vault over the Storage Access Framework — no cloud, no subscription.

## Features

- **SAF storage engine** — pick your Obsidian vault folder once; URI permission is persisted via `takePersistableUriPermission`.
- **Routes** — each route defines:
  - a target path with date tokens (`Daily/{{YYYY-MM-DD}}.md`, `{{date:yyyy/MM/dd}}`)
  - an insertion rule: **Append**, **Prepend**, or **Under header** (e.g. `# Tasks`)
  - a template wrapper (`- [ ] {{time:HH:mm}} {{content}}`)
  - Ships with three seeded routes: *Daily Note Append*, *Idea File*, *Inbox Task*.
- **Capture UI** — keyboard focused on launch, dark Material 3 theme, route dropdown, markdown formatting bar (bold, italic, strikethrough, code, quote, list, link).
- **Input modes** — text, native Android speech-to-text, and image capture with on-device **Google ML Kit Text Recognition** (works offline).
- **Home screen widget** (Jetpack Glance) — "New capture" opens a translucent quick-capture sheet; one-tap buttons send directly to your routes.

> Note on the widget: Android RemoteViews do not support editable text fields inside app widgets, so the widget's quick text entry opens a lightweight translucent `CaptureActivity` instead of an inline field.

## Architecture

Standard MVVM, single `app` module:

```
com.vaultdraft
├── domain
│   ├── model/Route.kt          # Route + InsertionRule
│   ├── tokens/TokenEngine.kt   # {{YYYY-MM-DD}}, {{time:HH:mm}}, {{timestamp}} expansion
│   └── capture/CaptureEngine.kt# append/prepend/under-header file building (pure, unit-tested)
├── data
│   ├── prefs/                  # DataStore (vault URI, routes JSON, defaults)
│   ├── repository/RouteRepository.kt
│   └── saf/VaultRepository.kt  # SAF read/write through DocumentFile
├── ui
│   ├── capture/                # CaptureScreen, CaptureViewModel, FormattingBar, SpeechInput, OcrInput
│   └── routes/                 # RoutesScreen (editor), RoutesViewModel
├── widget/VaultDraftWidget.kt  # Jetpack Glance home-screen widget
├── CaptureActivity.kt          # Translucent quick-capture (used by the widget)
└── MainActivity.kt / VaultDraftApp.kt
```

## Build

Requires JDK 17+ and Android Studio (Ladybug or newer). Open the project and run, or:

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

Dependencies: Jetpack Compose BOM + Material 3, Navigation Compose, DataStore Preferences, kotlinx-serialization, Glance 1.1.x, ML Kit text-recognition (on-device Latin script).

## Permissions

- `RECORD_AUDIO` — only used when you press the microphone button (system speech recognizer).
- No network permission — OCR runs fully on-device and all file writes go through SAF.

## License

MIT
