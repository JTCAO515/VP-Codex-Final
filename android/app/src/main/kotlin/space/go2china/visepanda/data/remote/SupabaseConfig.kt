package space.go2china.visepanda.data.remote

object SupabaseConfig {
    // 开启 mock 开关以便能在没有部署 Supabase 实例时进行模拟测试
    var MOCK_AUTH_ENABLED = true

    const val SUPABASE_URL = "https://your-project.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.dummy-anon-key"
}
