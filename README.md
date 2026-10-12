# Signal Listener

Android app that listens to Telegram channel notifications, parses trading signals,
and (in later phases) forwards them to MT5 via Accessibility Service or an HTTP API.

## Phases
- Phase 1-2: Notification listener + Signal parser ✅
- Phase 3: Channel list with enable/route UI ✅
- Phase 4: Sound + vibration alert ✅
- Phase 5: MT5 automation (pending)
- Phase 6: HTTP API sender (pending)

## Build
APK built automatically via GitHub Actions on every push to `main`.
Download from **Actions → latest run → Artifacts → app-debug**.
