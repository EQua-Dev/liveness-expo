package tech.sourceid.sdk.liveness.ui

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import tech.sourceid.sdk.liveness.data.LivenessApiConfig
import tech.sourceid.sdk.liveness.data.LivenessUIConfig
import tech.sourceid.sdk.liveness.network.SessionStatusChecker
import kotlin.concurrent.thread

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

    /** Session status required before the capture flow is allowed to start. */
    private const val STATUS_CREATED = "CREATED"
    private const val STATUS_EXPIRED = "EXPIRED"

    /**
     * Starts the liveness capture flow.
     *
     * When [apiConfig] is provided, the session's status is first verified
     * against the gateway and the flow only launches if the status is
     * `CREATED`; any other status (already used, expired, ...) is reported
     * through [onError] without opening the camera. When [apiConfig] is null
     * the check is skipped and the flow launches directly.
     */
    fun launch(
        context: Context,
        sessionId: String,
        region: String = "us-east-1",
        config: LivenessUIConfig,
        apiConfig: LivenessApiConfig? = null,
        onSuccess: ((String) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        callback = {
            when (it) {
                is LivenessResult.Success -> onSuccess?.invoke(it.message)
                is LivenessResult.Error -> onError?.invoke(it.message)
            }
        }

        if (sessionId.isBlank()){// || region.isBlank()) {
            notifyResult(LivenessResult.Error("sessionId is required"))
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

        if (apiConfig == null) {
            context.startActivity(intent)
            return
        }

        val mainHandler = Handler(Looper.getMainLooper())
        thread(name = "LivenessSessionStatus") {
            val result = SessionStatusChecker.fetchStatus(apiConfig, sessionId)
            mainHandler.post {
                result.fold(
                    onSuccess = { status ->
                        if (status.equals(STATUS_CREATED, ignoreCase = true)) {
                            context.startActivity(intent)
                        } else if (status.equals(STATUS_EXPIRED, ignoreCase = true)) {
                            notifyResult(
                                LivenessResult.Error(
                                    "Session cannot be used (status: $status). Generate a new session."
                                )
                            )
                        }else {
                            notifyResult(
                                LivenessResult.Error(
                                    "Session cannot be used (status: $status). Generate a new session."
                                )
                            )
                        }
                    },
                    onFailure = { error ->
                        // The gateway also 400s for sessions that were already
                        // run, so a failed check means "don't open the camera".
                        notifyResult(
                            LivenessResult.Error(
                                "Session not usable (${error.message}). " +
                                    "Generate a new session and try again."
                            )
                        )
                    }
                )
            }
        }
    }

    internal fun notifyResult(result: LivenessResult) {
        callback?.invoke(result)
        callback = null // Avoid memory leaks
    }

    // Whether a launch is still awaiting its result (used to detect user cancellation).
    internal val hasPendingCallback: Boolean
        get() = callback != null
}
