package tech.sourceid.sdk.liveness.ui

import android.content.Context
import android.content.Intent
import tech.sourceid.sdk.liveness.data.LivenessUIConfig

data class LivenessLaunchParams(
    val sessionId: String,
    val region: String,
    val config: LivenessUIConfig
)

sealed class LivenessResult {
    data class Success(val message: String = "Completed") : LivenessResult()
    data class Error(val message: String) : LivenessResult()
}

object LivenessSDK {
    private var callback: ((LivenessResult) -> Unit)? = null

    fun launch(
        context: Context,
        sessionId: String,
        region: String,
        config: LivenessUIConfig,
        onSuccess: ((String) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        callback = {
            when (it) {
                is LivenessResult.Success -> onSuccess?.invoke(it.message)
                is LivenessResult.Error -> onError?.invoke(it.message)
            }
        }

        if (sessionId.isBlank() || region.isBlank()) {
            notifyResult(LivenessResult.Error("sessionId and region are required"))
            return
        }

        val intent = Intent(context, LivenessActivity::class.java).apply {
            putExtra("sessionId", sessionId)
            putExtra("region", region)
            putExtra("customTitle", config.customTitle)
            putExtra("theme", config.theme)
            putExtra("primaryColorHex", config.primaryColorHex)
            putExtra("hideBranding", config.hideBranding)
        }

        context.startActivity(intent)
    }

    internal fun notifyResult(result: LivenessResult) {
        callback?.invoke(result)
        callback = null // Avoid memory leaks
    }

    // Whether a launch is still awaiting its result (used to detect user cancellation).
    internal val hasPendingCallback: Boolean
        get() = callback != null
}
