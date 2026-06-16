package com.visepanda.network

object ApiConfig {
    const val BASE_URL = "https://www.go2china.space"

    // API Endpoints
    const val CITIES = "/api/cities"
    const val MAP = "/api/map"
    const val CHAT = "/api/chat"
    const val TOOLS = "/api/tools"

    // Timeouts
    const val CONNECT_TIMEOUT = 15L
    const val READ_TIMEOUT = 60L
    const val WRITE_TIMEOUT = 30L
}
