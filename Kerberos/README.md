# Kerberos — Password Manager

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

     

  

