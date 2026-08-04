# SID Liveness SDK (Android)

The **SourceID Liveness SDK** provides a fast, secure, and customizable way to integrate **face liveness detection** into your Android application. It wraps the [AWS Amplify Face Liveness](https://ui.docs.amplify.aws/android/connected-components/liveness) detector (Amazon Rekognition Face Liveness) and adds SourceID session handling, theming, and branding.

---

## Features

* Real-time face liveness detection (AWS Rekognition Face Liveness)
* Customizable UI — theme, title, primary color, branding footer
* Automatic AWS Amplify initialization (no host-app setup required)
* Runtime camera-permission handling with error callbacks
* Guaranteed single callback — including user cancellation
* Kotlin + Jetpack Compose

---

## Installation

The SDK is published via [JitPack](https://jitpack.io).

**1. Add the JitPack repository** to your `settings.gradle(.kts)`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

**2. Add the dependency** to your app-level `build.gradle(.kts)`:

```kotlin
dependencies {
    implementation("com.github.EQua-Dev:liveness-expo:v1.5.0")
}
```

> **Note:** the artifact is currently published from the `EQua-Dev` mirror. Once the official `sourceidtechorg/sid-liveness-sdk-android` repository is public, the coordinate becomes `com.github.sourceidtechorg:sid-liveness-sdk-android:<tag>` — the API is identical.

**3. Declare the camera permission** in your app's `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.CAMERA" />
```

The SDK requests the permission at runtime for you; the manifest entry is still required.

---

## Requirements

| Requirement | Version |
| --- | --- |
| **minSdk** | 24 (Android 7.0) |
| **compileSdk** | 36 |
| **Kotlin** | 2.x |
| **Jetpack Compose** | required (the SDK UI is Compose-based) |

---

## Prerequisite: a liveness session ID

A liveness check runs against a **session** created server-side with Amazon Rekognition (`CreateFaceLivenessSession`). Your backend (e.g. the SourceID gateway's `POST /liveness/generate-liveness-url`) creates the session and returns the `sessionId`; after the flow completes, your backend fetches the confidence result (`GetFaceLivenessSessionResults`).

The SDK **never scores the check itself** — it runs the capture flow. `onSuccess` means the user completed the flow; your backend decides pass/fail from the session results.

---

## Usage

Launch the flow from any `Activity`, `Fragment`, or Compose click handler:

```kotlin
import tech.sourceid.sdk.liveness.ui.LivenessSDK
import tech.sourceid.sdk.liveness.data.LivenessUIConfig

LivenessSDK.launch(
    context = this,
    sessionId = sessionIdFromYourBackend,
    region = "us-east-1",
    config = LivenessUIConfig(
        hideBranding = false,
        customTitle = "Face Verification",
        theme = "dark",
        primaryColorHex = "#0A84FF"
    ),
    onSuccess = { message ->
        // The user completed the capture flow.
        // Fetch the session result from your backend to decide pass/fail.
    },
    onError = { error ->
        // Permission denied, cancelled, session/network failure, ...
    }
)
```

### Pre-flight session status check (optional)

Liveness sessions are single-use and short-lived. To avoid opening the camera
for a session that was already used or has expired, pass a `LivenessApiConfig`
— the SDK then asks the SourceID gateway for the session's status first and
only launches the capture flow when the status is `CREATED`:

```kotlin
import tech.sourceid.sdk.liveness.data.LivenessApiConfig

LivenessSDK.launch(
    context = this,
    sessionId = sessionIdFromYourBackend,
    region = "us-east-1",
    config = LivenessUIConfig(theme = "dark"),
    apiConfig = LivenessApiConfig(
        baseUrl = "https://<your-gateway-host>/v1/api",
        apiKey = yourApiKey,          // x-api-key header
        bearerToken = freshUserToken  // Authorization header; tokens expire
    ),
    onSuccess = { /* ... */ },
    onError = { error ->
        // Also fires when the status check fails, e.g.
        // "Session cannot be used (status: COMPLETED). Generate a new session."
    }
)
```

The check calls `POST {baseUrl}/liveness/liveness-result` with the session id
as the `reference`. Omit `apiConfig` to skip the check and launch directly
(previous behaviour).

### Callbacks

| Callback | When it fires |
| --- | --- |
| `onSuccess(message)` | The capture flow completed. Verify the result server-side. |
| `onError(message)` | Anything else — see the table below. |

Exactly **one** callback fires per `launch`. Notable `onError` messages:

| Message | Meaning |
| --- | --- |
| `"Liveness check cancelled"` | The user backed out of the flow before completing it. |
| `"Camera permission denied"` | The user declined the runtime camera permission. |
| `"sessionId and region are required"` | `launch` was called with blank arguments. |
| `"Amplify initialization failed: ..."` | AWS Amplify could not be configured. |
| Anything else | Propagated from the AWS Face Liveness detector (network, expired session, ...). |

---

## UI customization

`LivenessUIConfig`:

| Property | Type | Default | Description |
| --- | --- | --- | --- |
| `hideBranding` | `Boolean` | `false` | Hide the "Powered by SourceID" footer |
| `customTitle` | `String?` | `null` | Title shown above the camera view (omitted when `null`) |
| `theme` | `String` | `"light"` | `"light"` or `"dark"` |
| `primaryColorHex` | `String?` | `null` | Accent color, e.g. `"#0A84FF"` (falls back to theme default when null/invalid) |

---

## AWS configuration

The SDK bundles its own `amplifyconfiguration.json` (SourceID's Cognito identity pool) and initializes Amplify automatically on first launch of the flow — your app does not need any Amplify setup.

* If your app **already uses Amplify**, the SDK detects the existing configuration and reuses it.
* To point the SDK at **your own Cognito pool**, place an `amplifyconfiguration.json` in your app's `res/raw/` — app resources override the library's copy.

---

## Publishing (maintainers)

Releases are consumed through JitPack, which builds from git tags:

```bash
git tag v1.5.0
git push origin v1.5.0        # or the appropriate remote
```

The maven coordinates come from the repository (`com.github.<owner>:<repo>:<tag>`); the `maven-publish` block in `liveness/build.gradle.kts` supplies the POM metadata. To verify a build locally:

```bash
./gradlew :liveness:assembleRelease :liveness:publishReleasePublicationToMavenLocal
```

---

## License

MIT © SourceID

## Support

📧 [dev@sourceid.tech](mailto:dev@sourceid.tech) · 🌐 [https://sourceid.tech](https://sourceid.tech)
