# WABug Control

Android app containing:
- Front/rear camera live preview.
- Rear flash toggle when supported.
- In-app password screen with custom text.

## Build in Termux

Install Java and build tools:
```sh
pkg update
pkg install openjdk-17 git wget unzip -y
```

Install Android command-line SDK tools and Gradle, configure `ANDROID_HOME`, install platform 35 and build-tools 35.0.0, then run:
```sh
gradle assembleDebug
```
APK output:
`app/build/outputs/apk/debug/app-debug.apk`

## Notes
- This is an in-app lock, not a replacement for Android's device PIN or lock screen.
- Camera permission is requested at runtime.
- Flash requires a compatible rear camera.
- Password is stored in app-private preferences for this basic local app; it is not designed as a high-security vault.
