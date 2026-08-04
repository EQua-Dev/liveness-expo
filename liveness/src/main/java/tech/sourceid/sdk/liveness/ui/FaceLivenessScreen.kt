package tech.sourceid.sdk.liveness.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import android.util.Log
import com.amplifyframework.ui.liveness.model.FaceLivenessDetectionException
import com.amplifyframework.ui.liveness.ui.FaceLivenessDetector
import tech.sourceid.liveness.R
import tech.sourceid.sdk.liveness.data.LivenessUIConfig
import tech.sourceid.sdk.liveness.ui.theme.buildColorScheme

/**
 * [FaceLivenessDetectionException] is not a [Throwable], so its default
 * toString() is just "ClassName@hash". Build a readable message from its
 * actual parts instead.
 */
internal fun FaceLivenessDetectionException.toReadableMessage(): String {
    if (this is FaceLivenessDetectionException.UserCancelledException) {
        return "Liveness check cancelled"
    }
    val kind = this::class.simpleName ?: "FaceLivenessDetectionException"
    return buildString {
        append("$kind: $message")
        if (recoverySuggestion.isNotBlank()) append(" — $recoverySuggestion")
        throwable?.message?.let { append(" (cause: $it)") }
    }
}


@Composable
fun FaceLivenessScreen(
    modifier: Modifier = Modifier,
    sessionId: String,
    region: String,
    config: LivenessUIConfig,
    onComplete: () -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            onError("Camera permission denied")
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(android.Manifest.permission.CAMERA)
        }
    }

    var showInstructions by remember { mutableStateOf(true) }

    if (hasCameraPermission) {
        MaterialTheme(colorScheme = buildColorScheme(config)) {
            if (showInstructions) {
                LivenessStartScreen(config = config) { showInstructions = false }
                return@MaterialTheme
            }
            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                val isDarkMode = config.theme == "dark"
                val brandColor = if (isDarkMode) {
                    Color.White.copy(alpha = 0.6f)
                } else {
                    Color.Black.copy(alpha = 0.6f)
                }
                if (config.customTitle != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = config.customTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = brandColor
                        )

                    }
                }
                // Main detector takes most of the space
                Box(modifier = Modifier.weight(1f)) {
                    FaceLivenessDetector(
                        sessionId = sessionId,
                        region = region,
                        // The SDK shows its own instruction page first.
                        disableStartView = true,
                        onComplete = { onComplete() },
                        onError = { error ->
                            Log.e("LivenessSDK", "Face liveness failed: ${error.message}", error.throwable)
                            onError(error.toReadableMessage())
                        }
                    )
                }

                // Conditional branding footer
                if (!config.hideBranding) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Text(
                                text = "Powered by ",
                                style = MaterialTheme.typography.titleLarge,
                                color = brandColor
                            )
                            Image(
                                modifier = Modifier.size(96.dp),
                                painter = painterResource(id = R.drawable.sid_logo),
                                contentDescription = null
                            )
                        }


                    }
                }
            }
        }
    } else {
        Text(
            text = "Camera permission required to continue",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}