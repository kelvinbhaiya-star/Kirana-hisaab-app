# Kirana Hisab — Native Android App (Kotlin + Jetpack Compose + Material 3 + Room)

## How to Build the `.apk`

### Option 1: Android Studio (Recommended)
1. Open this `android/` folder in **Android Studio** (Ladybug / Koala or newer).
2. Wait for Gradle Sync to finish.
3. Click **Build > Build Bundle(s) / APK(s) > Build APK(s)** (or run `./gradlew assembleDebug`).
4. The `copyDebugApk` task automatically copies `app-debug.apk` to the project root folder (`app-debug.apk`, also available at `app/build/outputs/apk/debug/app-debug.apk`).

### Option 2: GitHub Actions (No Local Android Studio Required)
1. Upload the contents of this `android/` folder to a GitHub repository.
2. Open the **Actions** tab and run **Build Kirana Hisab APK** (or push to `main`).
3. Download `KiranaHisab-debug-apk` from the workflow artifacts.
