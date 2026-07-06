package space.jtcao.vpworkbuddy.data.api

/**
 * VisePanda API configuration.
 * Base URL points to the production backend on Vercel.
 */
object ApiConfig {
    /** Production API base URL (Vercel) */
    const val BASE_URL = "https://go2china.space"

    /** Request timeout in seconds */
    const val TIMEOUT_SECONDS = 30L

    /** SSE read timeout — streams can be long-lived */
    const val SSE_READ_TIMEOUT_SECONDS = 120L
}
