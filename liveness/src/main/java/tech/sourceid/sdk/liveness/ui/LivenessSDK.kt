package tech.sourceid.sdk.liveness.ui

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import tech.sourceid.sdk.liveness.data.LivenessApiConfig
import tech.sourceid.sdk.liveness.data.LivenessError
import tech.sourceid.sdk.liveness.data.LivenessUIConfig
import tech.sourceid.sdk.liveness.network.SessionStatusChecker
import tech.sourceid.sdk.liveness.network.StatusCheckOutcome
import kotlin.concurrent.thread

data class LivenessLaunchParams(
    val sessionId: String,
    val region: String,
    val config: LivenessUIConfig
)

sealed class LivenessResult {
    data class Success(val message: String = "Completed") : LivenessResult()
    data class Error(val error: LivenessError) : LivenessResult()
}

object LivenessSDK {
    internal const val TAG = "LivenessSDK"

    private var callback: ((LivenessResult) -> Unit)? = null

    /** Session status required before the capture flow is allowed to start. */
    private const val STATUS_CREATED = "CREATED"

    /**
     * Starts the liveness capture flow.
     *
     * When [apiConfig] is provided, the session's status is first verified
     * against the gateway and the flow only launches if the status is
     * `CREATED`; any other status (already used, expired, ...) is reported
     * through [onError] without opening the camera. When [apiConfig] is null
     * the check is skipped and the flow launches directly.
     *
     * [onError] receives a [LivenessError]: show `userMessage` to the user,
     * log `debugMessage` (the SDK already logs it under the `LivenessSDK`
     * tag), branch on `code` programmatically.
     */
    fun launch(
        context: Context,
        sessionId: String,
        region: String = "us-east-1",
        config: LivenessUIConfig,
        apiConfig: LivenessApiConfig? = null,
        onSuccess: ((String) -> Unit)? = null,
        onError: ((LivenessError) -> Unit)? = null
    ) {
        callback = {
            when (it) {
                is LivenessResult.Success -> onSuccess?.invoke(it.message)
                is LivenessResult.Error -> onError?.invoke(it.error)
            }
        }

        if (sessionId.isBlank()) {
            notifyResult(
                LivenessResult.Error(
                    LivenessError(
                        code = LivenessError.INVALID_ARGUMENTS,
                        userMessage = "Verification could not start. Please go back and try again.",
                        debugMessage = "launch() called with a blank sessionId"
                    )
                )
            )
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
            val outcome = SessionStatusChecker.fetchStatus(apiConfig, sessionId)
            mainHandler.post {
                when (outcome) {
                    is StatusCheckOutcome.Status -> {
                        if (outcome.value.equals(STATUS_CREATED, ignoreCase = true)) {
                            context.startActivity(intent)
                        } else {
                            notifyResult(
                                LivenessResult.Error(
                                    LivenessError(
                                        code = LivenessError.SESSION_NOT_USABLE,
                                        userMessage = "This verification session has already been used or has expired. Please start a new check.",
                                        debugMessage = "Gateway reported session status \"${outcome.value}\"; launch requires \"$STATUS_CREATED\""
                                    )
                                )
                            )
                        }
                    }
                    is StatusCheckOutcome.GatewayRejected -> {
                        // The gateway also 400s for sessions that were already
                        // run, so a rejection means "don't open the camera".
                        notifyResult(
                            LivenessResult.Error(
                                LivenessError(
                                    code = LivenessError.SESSION_NOT_USABLE,
                                    userMessage = "This verification session has already been used or has expired. Please start a new check.",
                                    debugMessage = "Gateway rejected the status check: HTTP ${outcome.httpCode} — ${outcome.gatewayMessage ?: "no message"}"
                                )
                            )
                        )
                    }
                    is StatusCheckOutcome.Unreachable -> {
                        notifyResult(
                            LivenessResult.Error(
                                LivenessError(
                                    code = LivenessError.STATUS_CHECK_FAILED,
                                    userMessage = "We couldn't verify your session. Please check your internet connection and try again.",
                                    debugMessage = "Status check failed before reaching a verdict: ${outcome.detail}"
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    internal fun notifyResult(result: LivenessResult) {
        if (result is LivenessResult.Error) {
            Log.e(TAG, "Liveness failed ${result.error}")
        }
        callback?.invoke(result)
        callback = null // Avoid memory leaks
    }

    // Whether a launch is still awaiting its result (used to detect user cancellation).
    internal val hasPendingCallback: Boolean
        get() = callback != null
}
