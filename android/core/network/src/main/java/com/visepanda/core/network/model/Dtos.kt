package com.visepanda.core.network.model

// ── Auth ──
data class RegisterRequest(val email: String, val password: String, val display_name: String? = null)
data class RegisterResponse(val id: String, val email: String, val display_name: String?, val status: String, val message: String)
data class LoginRequest(val email: String, val password: String)
data class TokenResponse(val access_token: String, val token_type: String)
data class VerifyEmailRequest(val token: String)
data class ForgotPasswordRequest(val email: String)
data class ResetPasswordRequest(val token: String, val new_password: String)
data class UserResponse(val id: String, val email: String, val display_name: String?, val role: String, val status: String)

// ── Destination ──
data class DestinationItem(
    val id: String, val name: String, val name_cn: String,
    val description: String, val image_url: String?,
    val tags: List<String>, val best_days: Int, val budget_range: String?,
)
data class DestinationListResponse(val destinations: List<DestinationItem>, val total: Int)
data class DestinationDetail(
    val id: String, val name: String, val name_cn: String,
    val description: String, val image_url: String?,
    val tags: List<String>,
    val must_see: List<MustItem>, val must_eat: List<MustItem>,
    val stay_tips: String?, val best_days: Int, val budget_range: String?,
    val latitude: Double?, val longitude: Double?,
)
data class MustItem(val name: String, val description: String, val image_url: String?)

// ── Chat ──
data class ChatRequest(val message: String, val city_context: String? = null)

// ── Trip ──
data class TripCreateRequest(val title: String, val cities: List<String>, val days: Int, val content: Any)
data class TripResponse(val id: String, val title: String, val cities: List<String>, val days: Int, val content: Any)
data class TripListResponse(val trips: List<TripResponse>, val total: Int)
