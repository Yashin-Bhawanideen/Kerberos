# Kerberos — Password Manager

# Scope and Purpose of the Kerberos Password Manager
Users can save and manage login credentials for their websites and apps in a single protected vault with Kerberos, a safe password management app for Android. Its goal is to make robust security simple to use, especially for individuals with numerous online accounts, in order to address frequent issues like password reuse and insecure storage. A Kotlin Android client, an ASP.NET Core REST API protected by HTTPS and JWT tokens, a PostgreSQL database, and a local Room database on the device for offline use comprise the three-tier system covered by the scope. Users of that system can use fingerprint or face recognition to unlock the vault after logging in using Google Single Sign-On. Credentials (service name, username or email, password, website URL, and optional notes) can be created, viewed, edited, and deleted; passwords are automatically concealed. Additionally, the app has a settings page, support for at least two South African languages, a customisable password generator, Android Autofill recommendations, offline access with automatic syncing once the connection is restored, and real-time security notifications (like a new device being authorised) that never display passwords.

# Structural Design and Architectural choice
Kerberos employs a three-tier architecture that divides the PostgreSQL database, the Android client, and the ASP.NET Core REST API (written in C#). This allows each layer to have distinct responsibilities and be independently developed, secured, and maintained. A user interface and navigation layer, ViewModels that store screen state and handle user actions, and a repository layer that interacts with the REST API, manages authentication and synchronisation, and exchanges JSON over HTTPS comprise the client's Kotlin application. Instead of using proprietary solutions, Android uses its own biometric and Autofill frameworks for device-level security. Since offline support is a crucial design factor, vault data is stored in a local Room database. Each credential has a SyncStatus field that indicates local modifications for synchronisation once connectivity recovers, ensuring that data is accessible even in the event of a sync failure. Before reaching application services like the Credential and User services, requests go via authentication and authorisation middleware on the server side, which verifies the JWT (returning 401 or 403 if needed) and field validation (returning 400 for incorrect input). These services access PostgreSQL through Entity Framework Core, and the API returns JSON with appropriate HTTP status codes. Security is built into the data design as well, since Google SSO handles authentication, every credential is tied to a UserId so the API can ensure users only access their own data, passwords must be encrypted, and notifications and autofill never expose passwords. Finally, Android localisation resources keep user-facing text separate from the code, making it easy to add more languages in the future.

# ASP.NET API Cloud Host
Hosted on Azure - account controlled by Mishal Bhikha

# Backend Database Host
Hosted on Firebase - account controlled by Yashin Bhawanideen

# Ensure that the firebase-service.json is in this path:
C:\Users\Kerberos\api\Kerberos.Api

# Ensure the the google-services.json is in this path:
C:\Users\Kerberos\android\app

# First time opening the project
1. Open Android Studio.
2. File → Open → navigate to the android/ folder inside the extracted zip (not the root Kerberos/ folder — open android/ specifically, since that's the actual Gradle project root). {Kerberos/android} > open the android file
3. Android Studio will detect the Gradle files and start syncing automatically. You'll see a progress bar at the bottom labeled "Gradle sync" or similar.

# First time running the application, this include the API + Android Studio
1. To run the project, first start the API: open a terminal or cmd, navigate into the API folder with `cd C:\Users\Downloads\Kerberos\api\Kerberos.Api`, then run `dotnet restore` followed by `dotnet run`. Leave this terminal open — once it prints "Now listening on: http://localhost:5000" (or whichever port it picks), the API is live and needs to keep running the whole time you're testing.
2. Next, open the Android project in Android Studio (the `android/` folder), and edit `app/src/main/java/com/example/kerberos/network/RetrofitClient.kt` to set `BASE_URL` to `http://10.0.2.2:5000/` if you're using the emulator (or your PC's local network IP, like `http://192.168.1.42:5000/`, if testing on a physical device on the same Wi-Fi). Since the API runs over plain HTTP locally, also open `app/src/main/AndroidManifest.xml` and temporarily set `android:usesCleartextTraffic="true"` so Android doesn't block the connection. 
3. Then sync Gradle (**File → Sync Project with Gradle Files**) and hit Run to launch the app on your emulator or device — with the API terminal still running in the background, sign in with Google and add a test credential to confirm the whole chain (Android app → API → Firestore) is working end to end.

# FI: 
If you change code or add code, always sync the project (because android studio likes to be difficult), before you run the program.
If you don't see the sync button check (**File → Sync Project with Gradle Files**)

# Firebase Database
You guys have access to the database, you don't need to edit anything in the database regarding adding a user or login details, the API will create the forms or data automatically to the database when a user signs into the app. Otherwise if you need to do neccessary changes you may do so as an 'Editor' of the database. 

# Folder Structure

**Kerberos**
    .gitignore
    README

    android
      .gradle
      .idea
      .kotlin
      app
      build
      gradle
      build.gradle
      gradle(properties)
      gradlew(file)
      gradlew(windows batch file)
      local
      settings.gradle

    api
      firestore.rules
      Kerberos.Api
        bin
        Controllers
        Models
        obj
        Services
        appsettings.json
        firebase-service-account.json
        Kerberos.Api.csproj
        Program.cs

     

  

