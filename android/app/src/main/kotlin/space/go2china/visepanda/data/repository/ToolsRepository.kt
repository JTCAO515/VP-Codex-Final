package space.go2china.visepanda.data.repository

import javax.inject.Inject
import javax.inject.Singleton
import space.go2china.visepanda.data.model.ToolCategory
import space.go2china.visepanda.data.model.ToolInteractiveDescriptor
import space.go2china.visepanda.data.remote.ExchangeRateApiService
import space.go2china.visepanda.data.remote.ExchangeRateResponse
import space.go2china.visepanda.data.tools.StaticToolsData

interface ToolsRepository {
    /** All tool categories; the "currency" category calculates only when live backend rates are available. */
    suspend fun getCategories(): List<ToolCategory>

    suspend fun getCategory(id: String): ToolCategory? = getCategories().find { it.id == id }
}

@Singleton
class LiveToolsRepository @Inject constructor(
    private val exchangeRateApiService: ExchangeRateApiService,
) : ToolsRepository {

    override suspend fun getCategories(): List<ToolCategory> {
        val liveRates = fetchLiveRates()
        if (liveRates == null) return StaticToolsData.categories
        return StaticToolsData.categories.map { category ->
            if (category.id == "currency") injectLiveRates(category, liveRates) else category
        }
    }

    private suspend fun fetchLiveRates(): ExchangeRateResponse? =
        runCatching { exchangeRateApiService.getExchangeRates() }
            .getOrNull()
            ?.takeIf { it.ok && it.rates.isNotEmpty() }

    private fun injectLiveRates(category: ToolCategory, data: ExchangeRateResponse): ToolCategory {
        val descriptor = category.interactive as? ToolInteractiveDescriptor.CurrencyConverter ?: return category
        return category.copy(
            interactive = descriptor.copy(
                rates = data.rates,
                ratesAreLive = true,
            ),
        )
    }
}
