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

## Next Validation Steps

1. Sync and build in Android Studio.
2. Add the missing Gradle wrapper from a JDK-enabled machine if your IDE does not generate it automatically.
3. Test notification parsing with WhatsApp, Gmail, and Calendar notifications.
4. Verify reminder behavior across reboot and app update.
