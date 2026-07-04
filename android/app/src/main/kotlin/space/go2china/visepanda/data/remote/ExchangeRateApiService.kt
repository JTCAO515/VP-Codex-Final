package space.go2china.visepanda.data.remote

import retrofit2.http.GET

/**
 * Mirrors the web app's `/api/exchange-rate` response shape. `ok = false`
 * means the backend has no `EXCHANGE_RATE_API_KEY` configured or the
 * upstream call failed — [space.go2china.visepanda.data.repository.ToolsRepository]
 * falls back to [space.go2china.visepanda.data.tools.StaticToolsData]'s
 * fixed rates in that case, same as the web app does.
 */
data class ExchangeRateResponse(
    val ok: Boolean,
    val base: String? = null,
    val rates: Map<String, Double> = emptyMap(),
    val updatedAt: String? = null,
)

interface ExchangeRateApiService {
    @GET("api/exchange-rate")
    suspend fun getExchangeRates(): ExchangeRateResponse
}
