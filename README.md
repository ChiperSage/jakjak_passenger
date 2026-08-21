# JakJak Passenger 🛵

Aplikasi pemesanan ojek online untuk **penumpang** — pasangan dari [jakjak_driver](../jakjak_driver).

## Tech Stack

| Layer | Library |
|---|---|
| Language | Kotlin |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 34 (Android 14) |
| Auth | Firebase Authentication |
| Database | Cloud Firestore |
| Storage | Firebase Storage |
| Notifikasi | FCM (Firebase Cloud Messaging) |
| UI | Material Components 3 |
| Async | Kotlin Coroutines |
| Architecture | MVVM + LiveData |
| Image | Glide |
| Build | Gradle 8.7 + AGP 8.3 + Kotlin DSL |

## Struktur Project

```
jakjak_passenger/
├── app/src/main/
│   ├── java/com/jakjak/passenger/
│   │   ├── JakJakPassengerApp.kt       ← Application class
│   │   ├── ui/
│   │   │   ├── splash/SplashActivity   ← Entry point, cek login
│   │   │   ├── auth/
│   │   │   │   ├── LoginActivity
│   │   │   │   └── RegisterActivity
│   │   │   └── home/HomeActivity       ← Utama + BottomNav
│   │   ├── viewmodel/
│   │   │   └── AuthViewModel           ← Login & register logic
│   │   └── utils/
│   │       ├── Constants.kt
│   │       ├── FirebaseHelper.kt
│   │       ├── Extensions.kt
│   │       ├── Resource.kt             ← UI state wrapper
│   │       ├── UserModel.kt
│   │       └── JakJakFirebaseMessagingService.kt
│   └── res/
│       ├── layout/                     ← XML layouts
│       ├── values/                     ← colors, strings, themes, dimens
│       ├── drawable/                   ← Vector icons
│       └── menu/                       ← Bottom nav menu
├── .github/workflows/android.yml       ← CI: debug + release APK
└── gradle/libs.versions.toml           ← Version catalog
```

## Setup

### 1. Clone & buka di Android Studio
```bash
git clone <repo-url>
cd jakjak_passenger
```

### 2. Firebase Setup
1. Buat project di [Firebase Console](https://console.firebase.google.com)
2. Tambahkan Android app → package: `com.jakjak.passenger`
3. Download `google-services.json` → letakkan di `app/`
4. Aktifkan: **Authentication** (Email/Password) · **Firestore** · **Storage** · **FCM**

### 3. Firestore Rules (minimal)
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    match /rides/{rideId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

### 4. Build
```bash
./gradlew assembleDebug
```

## CI/CD – GitHub Actions

Workflow `.github/workflows/android.yml` otomatis:
- **Push ke `develop`/`main`** → build debug APK + unit test
- **Push ke `main`** → build release APK (signed)

### GitHub Secrets yang dibutuhkan

| Secret | Isi |
|---|---|
| `GOOGLE_SERVICES_JSON` | `base64 app/google-services.json` |
| `KEYSTORE_BASE64` | `base64 release.jks` |
| `KEYSTORE_PASS` | Password keystore |
| `KEY_ALIAS` | Alias key |
| `KEY_PASS` | Password key |

```bash
# Cara encode secret:
base64 -w 0 app/google-services.json
base64 -w 0 release.jks
```

## Keterkaitan dengan jakjak_driver

| Fitur | Passenger | Driver |
|---|---|---|
| Package | `com.jakjak.passenger` | `com.jakjak.driver` |
| Role di Firestore | `passenger` | `driver` |
| Firestore `/users` | ✅ | ✅ |
| Firestore `/rides` | Buat order | Terima order |
| FCM topic | `passengers` | `drivers` |

## TODO / Next Steps

- [ ] Fragment Beranda + peta pemesanan
- [ ] Fragment Riwayat perjalanan
- [ ] Fragment Profil + edit foto
- [ ] Real-time ride tracking (Firestore listener)
- [ ] Rating driver setelah perjalanan
- [ ] Integrasi payment gateway
