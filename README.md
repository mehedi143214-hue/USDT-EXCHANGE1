# Crypto Trading App

An Android application built with Kotlin and Jetpack Compose for trading cryptocurrencies.

## Features
* User Authentication (Firebase)
* Real-time Cryptocurrency Rates
* Buy and Sell functionality
* Transaction History (Deposits, Orders)
* Admin Dashboard for managing rates and system maintenance
* Refer and Earn program

## Technologies Used
* Kotlin
* Jetpack Compose (UI)
* Firebase Authentication
* Firebase Firestore (Database)
* Coroutines & Flow
* Room Database (Local Persistence)

## Setup Instructions

1.  Clone the repository.
2.  Open the project in Android Studio.
3.  Set up Firebase:
    *   Create a Firebase project in the [Firebase Console](https://console.firebase.google.com/).
    *   Add an Android app to the project using the package name specified in `app/build.gradle.kts`.
    *   Download the `google-services.json` file and place it in the `app/` directory.
    *   Enable **Email/Password** authentication in Firebase Auth.
    *   Enable **Cloud Firestore** and set the security rules to allow authenticated users to read and write data:
        ```javascript
        rules_version = '2';
        service cloud.firestore {
          match /databases/{database}/documents {
            match /{document=**} {
              allow read, write: if request.auth != null;
            }
          }
        }
        ```
4.  Build and run the application on an emulator or a physical device.
