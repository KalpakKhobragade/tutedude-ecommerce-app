# TuteDude Shop - E-Commerce Android App

A basic e-commerce Android application built with **Kotlin**, **Jetpack Compose**, **MVVM**,
**Hilt**, **Room**, **Firebase** and **Retrofit** for the TuteDude assignment.

## Features

| Feature | Details |
|---|---|
| Authentication | Register and login with Firebase Authentication (email/password), with error messages for failures |
| Home screen | List of all products uploaded by users (image, title, short description, price) |
| Product details | Full description, image gallery, price, uploader name and contact, add to favorites |
| Upload product | Title, description, price, category and at least 3 images, saved to Firebase |
| Favorites | Stored locally with **Room**; available from the Favorites tab |
| Recommended | Products fetched from the **FakeStore API** with **Retrofit + Gson**; can be opened and favorited |
| Bonus | Search by text, category filter chips |

## Architecture (MVVM + Clean separation)

```
UI (Jetpack Compose screens)  ->  ViewModel (StateFlow)  ->  Repository  ->  Data sources
                                                                    |- Firebase Auth / Firestore / Storage
                                                                    |- Room (favorites)
                                                                    |- Retrofit (FakeStore API)
```

```
app/src/main/java/com/example/ecommerceapp/
├── EcommerceApp.kt          @HiltAndroidApp
├── MainActivity.kt          @AndroidEntryPoint, hosts the navigation graph
├── di/AppModule.kt          Hilt module (Firebase, Room, Retrofit)
├── data/
│   ├── model/               Product, mappers
│   ├── local/               Room: FavoriteEntity, FavoriteDao, AppDatabase
│   ├── remote/              Retrofit: FakeStoreApi, FakeProductDto
│   └── repository/          AuthRepository, ProductRepository, FavoriteRepository
├── ui/
│   ├── theme/               Material 3 theme
│   ├── components/          ProductCard, ProductImage, shared UI
│   ├── navigation/          AppNavGraph
│   ├── auth/                Login / Register + AuthViewModel
│   ├── home/                HomeScreen (3 tabs) + ViewModels
│   ├── details/             DetailsScreen + ViewModel
│   ├── upload/              UploadScreen + ViewModel
│   └── favorites/           Favorites tab + ViewModel
└── util/                    AppConfig, error messages, formatters
```

## How to run the project

### 1. Requirements
- Android Studio (latest stable) with an emulator that has **Google APIs / Google Play**, or a real phone
- A Google account for Firebase

### 2. Create the Firebase project
1. Go to https://console.firebase.google.com and click **Create a project**.
2. Click the Android icon to **add an Android app**. Package name: `com.example.ecommerceapp`
3. Download **google-services.json** and put it in the `app/` folder (next to `app/build.gradle.kts`).
4. **Build -> Authentication -> Get started -> Email/Password -> Enable -> Save.**
5. **Build -> Firestore Database -> Create database** (production mode), then open the
   **Rules** tab, paste the contents of [`docs/firestore.rules`](docs/firestore.rules) and click **Publish**.

### 3. Image storage
The project has a switch in `util/AppConfig.kt`:

```kotlin
val USE_FIREBASE_STORAGE: Boolean = false
```

- `false` (default): images are resized/compressed and saved inside Firestore. Works on the free Spark plan.
- `true`: images are uploaded to **Firebase Storage**. Google now requires the Blaze (billing) plan for Cloud Storage.
  Enable Storage in the console, publish [`docs/storage.rules`](docs/storage.rules), and set the flag to `true`.

### 4. Run
1. Open the project folder in Android Studio and wait for Gradle sync to finish.
2. Select an emulator or phone and click **Run**.

### Build an APK
**Build -> Build Bundle(s) / APK(s) -> Build APK(s)**. The file is created at
`app/build/outputs/apk/debug/app-debug.apk`.

## Tech stack
Kotlin, Jetpack Compose (Material 3), Navigation Compose, MVVM, Hilt, Room, Firebase Auth,
Firebase Firestore, Firebase Storage (optional switch), Retrofit + Gson, Coil, Kotlin Coroutines and Flow.

## Screenshots

| Login | Register | Home |
|---|---|---|
| ![](screenshots/login.png) | ![](screenshots/register.png) | ![](screenshots/home.png) |

| Details | Upload | Favorites | Recommended |
|---|---|---|---|
| ![](screenshots/details.png) | ![](screenshots/upload.png) | ![](screenshots/favorites.png) | ![](screenshots/recommended.png) |

## Author
Name: Kalpak Khobragade  |   Email: k.kalpak77@gmail.com
