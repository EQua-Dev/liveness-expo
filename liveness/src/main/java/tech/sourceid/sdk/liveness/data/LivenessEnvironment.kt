package tech.sourceid.sdk.liveness.data

/**
 * SourceID gateway environment. Consumers pass only the environment — the
 * SDK derives the gateway base URL internally.
 */
enum class LivenessEnvironment(internal val baseUrl: String) {
    PRODUCTION("https://api.usesourceid.com/v1/api"),
    SANDBOX("https://api.sbx.usesourceid.com/v1/api"),
    UAT("https://api.uat.usesourceid.com/v1/api"),
    DEVELOPMENT("https://api-rd.tailfaed50.ts.net/v1/api");

    companion object {
        /** Case-insensitive lookup, e.g. "production", "SANDBOX". */
        fun fromName(name: String?): LivenessEnvironment? =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
    }
}
