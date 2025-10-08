# SID Liveness SDK (Android)

The **SID Liveness SDK** provides a fast, secure, and customizable way to integrate **face liveness detection** into your Android application.
It enables seamless user verification while allowing full control over the UI theme and branding.

---

## 🚀 Features

* 🎥 Real-time face liveness detection
* 🎨 Customizable UI (theme, title, primary color, branding)
* 🧩 Easy integration into existing apps
* 🔒 Secure session handling via AWS Amplify
* ⚙️ Kotlin + Jetpack Compose support

---

## 📦 Installation

### 1. Add the SDK as a dependency

If you published it to a GitHub Packages / Maven repository:

```gradle
repositories {
    maven { url = uri("https://maven.pkg.github.com/sourceidtechorg/sid-liveness-sdk-android") }
    google()
    mavenCentral()
}

dependencies {
    implementation("tech.sourceid.sdk:livenesssdk:1.0.0")
}
```

If you are using it locally (as a module):

1. Copy the SDK folder (`livenesssdk/`) into your project root.
2. Add this line to your app-level `build.gradle`:

   ```gradle
   implementation(project(":liveness"))
   ```

---

## ⚙️ SDK Initialization

The SDK handles AWS Amplify initialization automatically — you don’t need to configure anything manually.

However, ensure that your app:

* Uses **Android 8.0 (API 26)** or higher
* Has **camera permission** in `AndroidManifest.xml`:

  ```xml
  <uses-permission android:name="android.permission.CAMERA" />
  ```

---

## 🧬 Usage

You can launch the **Liveness Verification Flow** from any `Activity` or `Fragment`:

```kotlin
import tech.sourceid.sdk.liveness.ui.LivenessSDK
import tech.sourceid.sdk.liveness.data.LivenessUIConfig

// Example: Launching the Liveness Flow
LivenessSDK.launch(
    context = this,
    sessionId = "your-session-id-here",
    region = "us-east-1",
    config = LivenessUIConfig(
        hideBranding = true,
        customTitle = "Face Verification",
        theme = "dark",
        primaryColorHex = "#FF5733"
    ),
    onComplete = {
        // ✅ Called when liveness check completes successfully
        Log.i("Liveness", "Liveness flow completed successfully!")
    },
    onError = { error ->
        // ❌ Called when an error occurs
        Log.e("Liveness", "Error: $error")
    }
)
```

---

## 🎨 UI Customization

| Property          | Type                 | Description                                     |
| ----------------- | -------------------- | ----------------------------------------------- |
| `hideBranding`    | `Boolean`            | Hide or show the "Powered by SourceID" footer   |
| `customTitle`     | `String?`            | Custom text to display at the top of the screen |
| `theme`           | `"light"` / `"dark"` | Set UI theme mode                               |
| `primaryColorHex` | `String`             | Set the SDK’s accent/primary color              |

Example:

```kotlin
LivenessUIConfig(
    hideBranding = false,
    customTitle = "Verify Your Identity",
    theme = "light",
    primaryColorHex = "#0084FF"
)
```

---

## 🥉 Callbacks

| Callback     | Description                                                             |
| ------------ | ----------------------------------------------------------------------- |
| `onComplete` | Triggered when the liveness flow completes successfully                 |
| `onError`    | Triggered when there’s an error (network, session, or permission issue) |

---

## 🧪 Permissions

The SDK automatically requests camera permission at runtime.
If permission is denied, it shows a fallback message indicating that camera access is required.

---

## 🧱 Example Integration

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Button(onClick = {
                LivenessSDK.launch(
                    context = this@MainActivity,
                    sessionId = "example-session-id",
                    region = "us-east-1",
                    config = LivenessUIConfig(
                        hideBranding = false,
                        customTitle = "Face Liveness Check",
                        theme = "dark",
                        primaryColorHex = "#0A84FF"
                    ),
                    onComplete = { Log.i("Liveness", "✅ Verification complete") },
                    onError = { Log.e("Liveness", "❌ Error: $it") }
                )
            }) {
                Text("Start Liveness Check")
            }
        }
    }
}
```

---

## 📋 Requirements

| Requirement          | Version                        |
| -------------------- | ------------------------------ |
| **Android SDK**      | 26+                            |
| **Kotlin**           | 1.8+                           |
| **Gradle Plugin**    | 8.0+                           |
| **Compose Compiler** | Compatible with Kotlin version |

---

## 🗾 License

```
Copyright (c) 2025 SourceID

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0
```

---

### 💬 Support

For support or integration help:
📧 **[dev@sourceid.tech](mailto:dev@sourceid.tech)**
🌐 [https://sourceid.tech](https://sourceid.tech)
