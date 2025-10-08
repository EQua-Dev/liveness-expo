package tech.sourceid.sdktesters

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.amplifyframework.core.Amplify
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import tech.sourceid.sdk.liveness.data.LivenessUIConfig
import tech.sourceid.sdk.liveness.ui.LivenessLaunchParams
import tech.sourceid.sdk.liveness.ui.LivenessResult
import tech.sourceid.sdk.liveness.ui.LivenessSDK
import tech.sourceid.sdktesters.ui.theme.SDKTestersTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Amplify Auth
        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.configure(applicationContext)
            Log.i("MainActivity", "Amplify initialized successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Could not initialize Amplify", e)
        }

        enableEdgeToEdge()
        setContent {
            SDKTestersTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        val launcher = rememberLauncherForActivityResult(
                            contract = LivenessSDK.Contract()
                        ) { result ->
                            when (result) {
                                is LivenessResult.Success -> Log.i("App", result.message)
                                is LivenessResult.Error -> Log.e("App", result.message)
                            }
                        }

                        Button(onClick = {
                            launcher.launch(
                                LivenessLaunchParams(
                                    sessionId = "a12e9b8a-8799-4ded-91aa-dae843b7a9e1",
                                    region = "us-east-1",
                                    config = LivenessUIConfig(theme = "dark")
                                )
                            )
                        }) {
                            Text("Start Liveness")
                        }
                        /*
                                                FaceLivenessScreen(
                                                    sessionId = "c4f07f5c-9393-4c59-b86e-e2eef2e1040f",
                                                    region = "us-east-1",
                                                    config = LivenessUIConfig(
                                                        hideBranding = true,
                                                        customTitle = "Face Verification",
                                                        theme = "dark",
                                                        primaryColorHex = "#FF5733"
                                                    ),
                                                    onComplete = { Log.i("Liveness", "✅ Flow complete!") },
                                                    onError = { err -> Log.e("Liveness", "❌ Error: $err") }
                                                )*/
                    }
                }
            }
        }
    }
}
