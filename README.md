# Kerberos — Password Manager

# Memebers involved
- ST10444715 Keegan Ewan Tromp
- ST10434249 Richard Hein
- ST10443463 Yashin Bhawanideen
- ST10443090 Amir Muller
- ST10450208 Mishal Bhikha


# This GitHub that was used for version control & proper GitHub workflows that work + Contributors
The link provided is public for viewing: 
https://github.com/Yashin-Bhawanideen/Kerberos.git

<img width="1662" height="632" alt="image" src="https://github.com/user-attachments/assets/464d4dad-c6e1-42f4-8609-ddbe46106acb" />

# Tests that succeeded

<img width="1662" height="632" alt="image" src="https://github.com/user-attachments/assets/d6af15d4-4141-45a3-8e14-95b5a51ccbc9" />

# Demonstration Videos
Two demonstration videos are required for the final submission.

REST API Creation, Operation, and Deployment [ADD API VIDEO LINK] Should cover the structure of the ASP.NET Core REST API, how it operates, its communication with Firestore, and how it's deployed.

Kerberos Android Application [ADD APPLICATION VIDEO LINK] Should cover the completed app and its core functionality — Google Single Sign-On, settings, credential management, REST API integration, cloud data, and the user-defined features.

# Screenshots and Technical Evidence
# Google Sign-in

<img width="300" height="600" alt="image" src="https://github.com/user-attachments/assets/7872c6e0-84e9-461f-a679-d05b82609add" />

# Biometric System

<img width="300" height="600" alt="image" src="https://github.com/user-attachments/assets/35feb522-4d24-43fe-bfd2-e98ee5c81fc6" />

# Home/Main page

<img width="300" height="600" alt="image" src="https://github.com/user-attachments/assets/6c8b502d-cf61-4a85-8eab-df85b2b1e196" />

# Add Credential CRUD

<img width="300" height="600" alt="image" src="https://github.com/user-attachments/assets/29f0d9aa-fe54-494f-b9bd-b34d612cf411" />

# Firebase Firestore Database storing user credentials

<img width="1662" height="632" alt="image" src="https://github.com/user-attachments/assets/2b8a3abc-9e79-4553-bb16-2df74d7fd091" />

# Azure API Hosted

<img width="1901" height="652" alt="image" src="https://github.com/user-attachments/assets/52ab260b-2357-43c4-bebe-56784e04e753" />


# Overview
Kerberos is a native Android password manager that gives users a secure, convenient way to store and manage their login credentials — replacing unsafe habits like password reuse or storing credentials in plain text. Users can save, view, edit, and delete entries containing a service name, username or email, password, website URL, and optional notes, with required fields validated before anything is saved.
Beyond core credential management, Kerberos includes biometric vault protection, a customisable password generator, English and Afrikaans language support, and offline storage with automatic cloud synchronisation.

# Tech stack:
Android app: Kotlin
Authentication: Google Single Sign-On via Firebase Authentication
Local storage: Room database (offline support)
Backend API: ASP.NET Core REST API, hosted on Microsoft Azure
Cloud database: Firebase Firestore

# Architecture
Kerberos follows a layered architecture that cleanly separates the Android client, the REST API, and the cloud database:
Android (Kotlin) Application  →  ASP.NET Core REST API (Azure)  →  Firebase Firestore
Android application — UI and navigation, ViewModels for screen state, repositories for data operations, and a local Room database that keeps credentials available offline.
REST API — Built with ASP.NET Core and C#, organised into controllers, models, and services. The Android app communicates with it via Retrofit over HTTPS, using JSON requests. Every credential is tied to its authenticated owner, and requests carry an authentication token so the API can identify the correct user.

# Data flow:

Google Sign-In / Firebase Authentication
        ↓
Kerberos Android Application
        ↓
Local Room Database
        ↓
Retrofit REST Requests
        ↓
ASP.NET Core REST API (Azure)
        ↓
Firebase Firestore

The local database lets Kerberos keep working when there's no internet connection. Any change that still needs to reach the cloud is marked with a synchronisation state and picked up by the sync worker once connectivity returns.

# Authentication — Google Single Sign-On
Instead of creating a separate Kerberos-only account, users sign in with their existing Google account through Firebase Authentication, which also secures every request made to the REST API — keeping unauthenticated users out of protected credential operations. This flow has been verified on a physical Android device.

# Biometric Vault Protection
For an extra layer of security, users can enable biometric authentication from the Settings screen. When turned on, the vault stays locked until the user authenticates with a supported biometric method. This preference is saved and persists across app restarts.

Settings
The Settings screen lets users control:
Biometric vault protection
Notification preferences
Application language
Sign out
All settings are persisted and survive an app restart. Language support currently covers English and Afrikaans, implemented through Android's localisation resources so user-facing text can be translated without touching application logic.

# REST API
A dedicated ASP.NET Core REST API handles all credential operations, organised into controllers, models, and services for maintainability. It supports the full CRUD set required by the vault:
Create a credential
Read credentials
Update a credential
Delete a credential

# Offline Storage and Synchronisation
Credentials are stored locally with Room, which underpins Kerberos's offline functionality. Each credential carries synchronisation metadata indicating whether it still needs to be created, updated, or deleted in the cloud, and a background worker processes these pending changes against the REST API when a connection is available.
Synchronisation events are logged via Android logging for visibility during development and testing — the request bodies themselves are deliberately excluded from these logs to avoid exposing credential data.

#Credential Management
From inside the protected vault, users can:
Add credentials
View saved credentials
Edit credentials
Delete credentials
Show or hide passwords
Store credentials locally
Sync credentials to the cloud

# Password Generator
Kerberos includes a built-in, customisable password generator so users never have to leave the app to create a strong password. Users choose a length between 8 and 32 characters and select which character types to include:
Uppercase letters
Lowercase letters
Numbers
Symbols

# Key User-Defined Features
1. Offline Storage with Cloud Synchronisation Credentials live locally in Room and sync to Firestore through the custom REST API, with synchronisation states tracking which credentials still need cloud operations.
2. Multi-Language Support English and Afrikaans are supported, switchable from Settings, with the selection persisting across restarts.
3. Customisable Password Generator Users control both password length and character composition when generating new passwords.

# Getting Started — Android Project
Clone or download the Kerberos repository.
Open Android Studio.
Select File → Open.
Navigate to the Android Gradle project inside the Kerberos project.
Open the android folder.
Let Android Studio finish the Gradle sync.
Make sure a valid google-services.json is present in android/app/.
Connect a physical device or start an emulator.
Build and run the app.

By default, the app talks to the deployed Azure-hosted API, so you don't need to run the API locally just to test against the live backend.

# Running the API Locally
The API project lives at:
api/Kerberos.Api/
From that directory, run:
dotnet restore

# Conclusion
Kerberos is a secure Android password manager that pairs local, offline-first Android functionality with a custom cloud REST API. It brings together Google Single Sign-On, biometric vault protection, full credential CRUD, settings, localisation, password generation, offline Room storage, and cloud synchronisation through an ASP.NET Core API hosted on Azure and backed by Firebase Firestore.
The project was built and tested incrementally with Git and GitHub, supported by automated unit testing and GitHub Actions CI to catch regressions as the codebase evolved. Together, the Android application, local database, custom REST API, authentication service, and cloud database demonstrate a complete, security-conscious credential management system.
dotnet run

You'll need a valid Firebase service account configuration and the required application settings available locally. As always, never upload private keys or service account credentials to the public repository.

