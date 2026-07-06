package space.jtcao.visepanda.data.auth

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import space.jtcao.visepanda.BuildConfig

object SupabaseClientFactory {
    private val configuredClient: SupabaseClient? by lazy {
        if (!isConfigured()) {
            null
        } else {
            createSupabaseClient(
                supabaseUrl = BuildConfig.SUPABASE_URL.trim(),
                supabaseKey = BuildConfig.SUPABASE_ANON_KEY.trim()
            ) {
                install(Auth) {
                    val scheme = BuildConfig.SUPABASE_REDIRECT_SCHEME.trim()
                    val host = BuildConfig.SUPABASE_REDIRECT_HOST.trim()

                    if (scheme.isNotBlank() && host.isNotBlank()) {
                        this.scheme = scheme
                        this.host = host
                    }

                    alwaysAutoRefresh = true
                }

                install(Postgrest)
            }
        }
    }

    fun createOrNull(): SupabaseClient? = configuredClient

    fun redirectUrlOrNull(): String? {
        val scheme = BuildConfig.SUPABASE_REDIRECT_SCHEME.trim()
        val host = BuildConfig.SUPABASE_REDIRECT_HOST.trim()
        return if (scheme.isNotBlank() && host.isNotBlank()) {
            "$scheme://$host"
        } else {
            null
        }
    }

    private fun isConfigured(): Boolean =
        BuildConfig.SUPABASE_URL.trim().isNotBlank() &&
            BuildConfig.SUPABASE_ANON_KEY.trim().isNotBlank()
}
