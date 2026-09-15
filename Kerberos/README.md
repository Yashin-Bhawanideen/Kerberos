# Kerberos — Password Manager

Two projects in this archive:

- `android/` — the Kotlin/Jetpack Compose Android app (package `com.example.kerberos`).
  UI matches the provided design screens (login, vault/home, add credential, credential detail).
- `api/Kerberos.Api/` — the ASP.NET Core Web API that verifies Firebase ID tokens and is the
  only thing that reads/writes Firestore.

No Firebase project configuration is included in this archive (no `google-services.json`,
no `firebase-service-account.json`, no filled-in client IDs) — you handle that part yourself.
Everything else — all app code, screens, networking, and the API — is complete and ready to
build once you drop your own Firebase config in.

This version already has every fix worked out during setup: correct Gradle memory settings,
AndroidX enabled, Kotlin 2.0.21 (required by the Compose compiler plugin), and the corrected
Firestore credential wiring in the API. If you were hand-editing files from an earlier version
of this project, replace them with the ones in this archive rather than re-patching your copy.

## Architecture

```
Android app (Firebase Auth handles Google SSO)
  --> issues a Firebase ID token
  --> sent as "Authorization: Bearer <token>" on every API call
  --> ASP.NET Core API verifies the token
  --> API (via Firebase Admin SDK) reads/writes Firestore, scoped to the verified uid
```

The Android app never talks to Firestore directly. `api/firestore.rules` is a deny-all
rule set — only the API's service-account credentials can touch the database. Apply it
in the Firebase console when you set up your project.

## What you'll need to configure yourself (not included here)

1. A Firebase project, with an Android app registered under package `com.example.kerberos`.
   Its `google-services.json` goes in `android/app/`.
2. Authentication → Sign-in method → enable **Google**.
3. Grab the **Web Client ID** (Authentication → Sign-in method → Google → Web SDK
   configuration) and paste it into:
   `android/app/src/main/java/com/example/kerberos/auth/AuthRepository.kt`
   (replace the `YOUR_WEB_CLIENT_ID.apps.googleusercontent.com` placeholder).
4. A Firestore database, with `api/firestore.rules` applied.
5. Project Settings → Service Accounts → generate a private key → save as
   `api/Kerberos.Api/firebase-service-account.json` (must sit right next to `Program.cs`).
6. Set `Firebase:ProjectId` in `api/Kerberos.Api/appsettings.json` to your project ID.

## Opening the project

Open Android Studio → **File → Open** → select the `android` folder specifically
(not the top-level `Kerberos` folder — `android/` is the actual Gradle project root).
Let the initial Gradle sync finish before doing anything else.

## Running the API

```bash
cd api/Kerberos.Api
dotnet restore
dotnet run
```

Wait for it to print `Now listening on: http://localhost:5000` (or whichever port it
picks) and leave that terminal open — the API needs to stay running while you test the app.

## Running the Android app

1. Edit `android/app/src/main/java/com/example/kerberos/network/RetrofitClient.kt` and set
   `BASE_URL`:
   - Emulator talking to a locally-run API: `http://10.0.2.2:5000/`
   - Physical device on the same Wi-Fi: your PC's LAN IP, e.g. `http://192.168.1.42:5000/`
   - Deployed API: your real `https://` URL
2. Since local `dotnet run` serves plain HTTP, open
   `android/app/src/main/AndroidManifest.xml` and temporarily set
   `android:usesCleartextTraffic="true"` (set it back to `false` before shipping against
   a real HTTPS deployment).
3. **File → Sync Project with Gradle Files**, then **Run** on an emulator or device.
4. Sign in with Google, add a test credential, and confirm it appears — check the API's
   terminal for incoming request logs, and the Firestore console's Data tab for a new
   `users/{uid}/credentials` document.

## gradle.properties

This file is included at `android/gradle.properties` with the settings this project
needs:
```
org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=1024m
org.gradle.parallel=true
android.useAndroidX=true
```
If Android Studio still reports an AndroidX or out-of-memory error after opening this
project, check `%USERPROFILE%\.gradle\gradle.properties` (a *separate*, global file
outside this project) for conflicting settings — it can override or shadow the project-level
file above.

## What's implemented

- Google Single Sign-On (Firebase Auth) with a matching login screen, including the
  "Simulate sign-in error" affordance shown in the design
- Vault / home screen: search bar, stat cards, credential list
- Secure credential vault: create / view / list / delete
  (service name, username, password, URL, notes; password hidden by default with a
  reveal toggle; copy-to-clipboard on username/password/website; delete requires confirmation)
- Built-in password generator
- Full ASP.NET Core API with Firebase ID token verification and Firestore integration

## What's NOT yet implemented (still on the roadmap)

- Biometric authentication (vault unlock)
- Offline functionality & sync (Room + SQLCipher)
- Application settings screen (the settings icon in the header is wired for navigation
  but has no destination screen yet)
- Real-time notifications (Firebase Cloud Messaging) — the bell icon in the header is
  visual only right now
- Multi-language support
- Android Autofill integration
- Edit-credential screen (the Edit link on the detail screen is currently a placeholder)
- Master password sign-in (the button exists in the UI but has no logic behind it yet)
