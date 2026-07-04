package space.go2china.visepanda.data.remote

import com.google.gson.JsonElement
import retrofit2.http.GET
import retrofit2.http.Query

interface ExploreApiService {
    @GET("api/explore/amap")
    suspend fun searchAmapPois(
        @Query("cityId") cityId: String,
        @Query("type") type: String,
        @Query("keyword") keyword: String? = null
    ): ExploreAmapResponse
}

data class ExploreAmapResponse(
    val ok: Boolean,
    val cityId: String,
    val type: String,
    val pois: List<AmapPoiJson>
)

data class AmapPoiJson(
    val id: String,
    val name: String,
    val type: String,
    val address: JsonElement? = null,
    val tel: JsonElement? = null,
    val opentime_week: String? = null,
    val business_area: String? = null,
    val location: String? = null,
    val biz_ext: JsonElement? = null,
    val photos: List<AmapPhoto>? = null
) {
    fun getAddressValue(): String? {
        if (address == null || address.isJsonNull) return null
        return if (address.isJsonArray) {
            try {
                address.asJsonArray.firstOrNull { it.isJsonPrimitive }?.asString
            } catch (e: Exception) {
                null
            }
        } else {
            try {
                address.asString
            } catch (e: Exception) {
                null
            }
        }
    }

    fun getTelValue(): String? {
        if (tel == null || tel.isJsonNull) return null
        return if (tel.isJsonArray) {
            try {
                tel.asJsonArray.firstOrNull { it.isJsonPrimitive }?.asString
            } catch (e: Exception) {
                null
            }
        } else {
            try {
                tel.asString
            } catch (e: Exception) {
                null
            }
        }
    }

    fun getRating(): String? {
        if (biz_ext == null || biz_ext.isJsonNull || biz_ext.isJsonArray) return null
        return try {
            biz_ext.asJsonObject.get("rating")?.asString
        } catch (e: Exception) {
            null
        }
    }

    fun getCost(): String? {
        if (biz_ext == null || biz_ext.isJsonNull || biz_ext.isJsonArray) return null
        return try {
            biz_ext.asJsonObject.get("cost")?.asString
        } catch (e: Exception) {
            null
        }
    }
}

data class AmapPhoto(
    val url: String? = null,
    val title: String? = null
)
