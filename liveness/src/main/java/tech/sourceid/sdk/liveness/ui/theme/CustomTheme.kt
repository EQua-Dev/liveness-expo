package tech.sourceid.sdk.liveness.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.amplifyframework.ui.liveness.ui.LivenessColorScheme
import tech.sourceid.sdk.liveness.data.LivenessUIConfig

@Composable
fun buildColorScheme(config: LivenessUIConfig): ColorScheme {
    return when (config.theme.lowercase()) {
        "dark" -> LivenessColorScheme.Defaults.darkColorScheme.copy(
            primary = config.getPrimaryColor()
                ?: LivenessColorScheme.Defaults.darkColorScheme.primary
        )
        else -> LivenessColorScheme.Defaults.lightColorScheme.copy(
            primary = config.getPrimaryColor()
                ?: LivenessColorScheme.Defaults.lightColorScheme.primary
        )
    }
}
