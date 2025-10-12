package tech.sourceid.sdk.liveness.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
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
        // Store callback as a single entry point
        callback = {
            when (it) {
                is LivenessResult.Success -> onSuccess?.invoke(it.message)
                is LivenessResult.Error -> onError?.invoke(it.message)
            }
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

//        LivenessActivity.start(context, sessionId, region, config)
    }
    internal fun notifyResult(result: LivenessResult) {
        callback?.invoke(result)
        callback = null // Avoid memory leaks
    }

    // ✅ Host apps can use this with ActivityResult API
    /*class Contract : ActivityResultContract<LivenessLaunchParams, LivenessResult>() {
        override fun createIntent(context: Context, input: LivenessLaunchParams): Intent {
            return Intent(context, LivenessActivity::class.java).apply {
                putExtra("sessionId", input.sessionId)
                putExtra("region", input.region)
                putExtra("customTitle", input.config.customTitle)
                putExtra("theme", input.config.theme)
                putExtra("primaryColorHex", input.config.primaryColorHex)
                putExtra("hideBranding", input.config.hideBranding)
            }
        }

        override fun parseResult(resultCode: Int, intent: Intent?): LivenessResult {
            return when (resultCode) {
                Activity.RESULT_OK ->
                    LivenessResult.Success(intent?.getStringExtra("message") ?: "Success")
                Activity.RESULT_CANCELED ->
                    LivenessResult.Error(intent?.getStringExtra("error") ?: "Cancelled")
                else ->
                    LivenessResult.Error("Unknown result")
            }
        }
    }*/
}
