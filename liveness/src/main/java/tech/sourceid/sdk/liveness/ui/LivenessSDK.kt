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
    fun launch(
        context: Context,
        sessionId: String,
        region: String,
        config: LivenessUIConfig
    ) {
        LivenessActivity.start(context, sessionId, region, config)
    }

    // ✅ Host apps can use this with ActivityResult API
    class Contract : ActivityResultContract<LivenessLaunchParams, LivenessResult>() {
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
    }
}
