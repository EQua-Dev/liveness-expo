package tech.sourceid.sdk.liveness.data

/**
 * Connection details for the SourceID gateway, used to verify a liveness
 * session's status before the capture flow is launched.
 *
 * @property baseUrl Gateway API base, e.g. "https://api-rd.tailfaed50.ts.net/v1/api"
 * @property apiKey Value for the `x-api-key` header.
 * @property bearerToken Value for the `Authorization: Bearer` header. Tokens
 * expire — supply a fresh one per launch.
 */
data class LivenessApiConfig(
    val baseUrl: String,
    val apiKey: String,
    val bearerToken: String
)
