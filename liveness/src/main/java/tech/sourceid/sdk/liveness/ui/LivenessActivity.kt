package tech.sourceid.sdk.liveness.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import com.amplifyframework.core.Amplify
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import tech.sourceid.sdk.liveness.data.LivenessUIConfig
import tech.sourceid.sdk.liveness.ui.theme.SDKTestersTheme

class LivenessActivity : ComponentActivity() {

    companion object {
        fun start(
            context: Context,
            sessionId: String,
            region: String,
            config: LivenessUIConfig
        ) {
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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Amplify
        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.configure(applicationContext)
            Log.i("LivenessActivity", "Amplify initialized successfully")
        } catch (e: Exception) {
            Log.e("LivenessActivity", "Could not initialize Amplify", e)
        }

        val sessionId = intent.getStringExtra("sessionId") ?: ""
        val region = intent.getStringExtra("region") ?: ""
        val config = LivenessUIConfig(
            hideBranding = intent.getBooleanExtra("hideBranding", false),
            customTitle = intent.getStringExtra("customTitle"),
            theme = intent.getStringExtra("theme") ?: "light",
            primaryColorHex = intent.getStringExtra("primaryColorHex") ?: "#000000"
        )

        setContent {
            SDKTestersTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        FaceLivenessScreen(
                            sessionId = sessionId,
                            region = region,
                            config = config,
                            onComplete = {
                                val data = Intent().apply {
                                    putExtra("message", "Liveness flow completed successfully.")
                                }
                                setResult(Activity.RESULT_OK, data)
                                finish()
                            },
                            onError = { err ->
                                val data = Intent().apply {
                                    putExtra("error", err)
                                }
                                setResult(Activity.RESULT_CANCELED, data)
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }
}
