# Android APK wrapper

This project packages the deployed ACG Community web application as an Android APK using Capacitor.

- Application ID: `com.acg.community`
- App name: `漫化 ACG 社区`
- Runtime URL: `http://39.105.128.249`
- Debug APK: `android/app/build/outputs/apk/debug/app-debug.apk`

Build on Windows:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
npm install
npx cap sync android
cd android
./gradlew.bat assembleDebug --no-daemon
```

This is a debug-signed APK. For store distribution, set up a release keystore, HTTPS, and a production server URL before building a release variant.
