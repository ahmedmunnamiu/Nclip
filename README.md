# Nclip — Android notification & clipboard history

A minimal AMOLED-black Android app with square-style controls and local SQLite storage.

## Features
- Saves posted notifications after the user explicitly enables Android Notification Access.
- Saves text copied while Nclip is open/in the foreground.
- Clipboard entries can be copied again or deleted.
- Notification and clipboard histories can be cleared separately.
- Data remains in the app's private local database. No network permission or cloud upload is included.

## Important Android limitations
- Notification access must be granted manually in Android Settings.
- Android restricts background clipboard reads. Nclip listens for clipboard changes while its Activity is active; it cannot reliably capture every copy made in other apps while Nclip is backgrounded or closed. This is an OS privacy restriction, not a missing permission.
- Notification content may include private information. Review what you store and use the app only on your own device.

## Build APK with GitHub
1. Create a new GitHub repository.
2. Upload the **contents** of this ZIP to the repository root (the `.github` folder must also be at root).
3. Open **Actions** → **Build Nclip APK** → **Run workflow** (or push to `main`).
4. Open the completed workflow run and download the `Nclip-debug-apk` artifact.
5. Extract the artifact ZIP to get `app-debug.apk`.

## Enable notification capture
Install the APK, open Nclip, tap **ACCESS SETTINGS**, then enable Nclip under Notification Access. Android will show a system confirmation screen.

## About
Developer: Ahmed Munna  
Phone: 01625104847  
Facebook: https://facebook.com/ahmedmunna0  
GitHub: https://github.com/ahmedmunnamiu

Package: `com.ahmedmunna.nclip`
Min Android: Android 8.0 (API 26)
Target SDK: 35
