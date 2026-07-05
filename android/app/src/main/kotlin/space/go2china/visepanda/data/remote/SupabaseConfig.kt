package space.go2china.visepanda.data.remote

import space.go2china.visepanda.BuildConfig

/**
 * Supabase project configuration.
 *
 * Values are injected at build time via buildConfigField in app/build.gradle.kts.
 * Only SUPABASE_URL and SUPABASE_ANON_KEY (public by design) are present here.
 * SUPABASE_SERVICE_ROLE_KEY is server-side only and MUST NEVER appear in client code.
 */
object SupabaseConfig {
    val SUPABASE_URL: String
        get() = BuildConfig.SUPABASE_URL

    val SUPABASE_ANON_KEY: String
        get() = BuildConfig.SUPABASE_ANON_KEY
}
