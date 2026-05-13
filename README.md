# Offline Task Manager

Privacy-first Android task capture app that turns notifications and pasted meeting transcripts into actionable tasks using fully local processing.

## Project Structure

- `app`: application shell, navigation, onboarding intents, dashboard workflow
- `core`: shared Compose theme and small UI primitives
- `domain`: models, repository contracts, engine contracts, use cases
- `data`: Room entities and DAO, repositories, notification listener, reminders, exports, calendar and email integration
- `ml`: multilingual rule engine, due-date parser, transcript extraction, ONNX-ready extraction layer
- `feature-*`: Compose screens for tasks, inbox, analytics, settings, and transcripts

## Setup

1. Install Android Studio with Android SDK 35.
2. Install JDK 17 and ensure `JAVA_HOME` is configured.
3. Open the project root in Android Studio.
4. Sync Gradle and install the app on an Android 10+ device.
5. Grant notification access and disable battery optimizations during onboarding.

## Current Notes

- The source tree is fully scaffolded, but this environment did not have `java` or `gradle`, so the project could not be compiled here.
- Notification ingestion uses Android's `NotificationListenerService` only; no accessibility automation or cloud APIs are used.
- ONNX Runtime Mobile is included and the extraction layer is structured for model-backed scoring, with heuristic scoring used as the default baseline.

## GitHub Build Pipeline

- `.github/workflows/android-ci.yml` builds a debug APK on every push, pull request, and manual run.
- `.github/workflows/android-release.yml` can be triggered manually to build release artifacts and optionally publish them as a GitHub Release.
- If you do not configure signing secrets, the debug APK will still be installable from GitHub artifacts.
- If you configure signing secrets, the release workflow can also produce a signed release APK.

### Required GitHub Secrets For Signed Release APKs

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

### How To Use From Your Phone

1. Push this repo to GitHub.
2. Open the repository `Actions` tab in your mobile browser or the GitHub app.
3. Run `Android CI` to get a debug APK artifact.
4. Download the APK artifact and install it on your phone.
5. Once signing secrets are configured, run `Android Release APK` to publish a release build directly on GitHub.

## Next Validation Steps

1. Push the repo and run the GitHub Actions workflows.
2. Fix any CI compile failures surfaced by the first cloud build.
3. Test notification parsing with WhatsApp, Gmail, and Calendar notifications on-device.
4. Verify reminder behavior across reboot and app update.
