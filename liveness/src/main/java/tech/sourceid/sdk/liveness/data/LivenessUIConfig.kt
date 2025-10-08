package tech.sourceid.sdk.liveness.data

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

data class LivenessUIConfig(
    val hideBranding: Boolean = false,
    val customTitle: String? = null,
    val theme: String = "light",   // "light" | "dark"
    val primaryColorHex: String? = null
) {
    fun getPrimaryColor(): Color? {
        return primaryColorHex?.let {
            try {
                Color(it.toColorInt())
            } catch (e: IllegalArgumentException) {
                null // fallback if invalid hex
            }
        }
    }
}
