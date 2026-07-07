# VisePanda Native Android

Native Android version of `JTCAO515/VP-Codex-Web`.

Source web commit used for this native port: `4a58629`.

The app is implemented with Android platform APIs only: no WebView shell, no third-party runtime dependencies. It recreates the main VisePanda product surfaces with native controls:

- Plan command center
- Ask VisePanda chat client
- Cities explorer
- Travel tools
- Guest and authenticated trip drafts

The app talks to the existing production API at `https://go2china.space/api/*`. Guest trips are stored locally when the user is not signed in.

## Build

Open this repository in Android Studio and run:

```powershell
$env:ANDROID_HOME='C:\tmp\android-sdk'
$env:ANDROID_SDK_ROOT='C:\tmp\android-sdk'
gradle assembleDebug
```

The generated APK from this handoff is also stored at:

```text
release/VisePanda-native-debug.apk
```
