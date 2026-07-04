package space.go2china.visepanda.data.explore

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class ExploreCity(
    val id: String,
    val name: String,
    val region: String,
    val tagline: String,
    val bestFor: List<String>
)

data class ExploreLocation(
    val lat: Double,
    val lng: Double
)

open class ExploreRichMeta(
    open val rating: String? = null,
    open val pricePerPerson: String? = null,
    open val priceLevel: String? = null,
    open val tel: String? = null,
    open val openHours: String? = null,
    open val photoUrl: String? = null,
    open val businessArea: String? = null,
    open val sourceLabel: String? = null,
    open val location: ExploreLocation? = null,
    open val confidence: String = "High",
    open val fitRationale: String = ""
)

data class ExploreAttraction(
    val id: String,
    val cityId: String,
    val name: String,
    val category: String,
    val description: String,
    override val rating: String? = null,
    override val pricePerPerson: String? = null,
    override val priceLevel: String? = null,
    override val tel: String? = null,
    override val openHours: String? = null,
    override val photoUrl: String? = null,
    override val businessArea: String? = null,
    override val sourceLabel: String? = null,
    override val location: ExploreLocation? = null,
    override val confidence: String = "High",
    override val fitRationale: String = ""
) : ExploreRichMeta(rating, pricePerPerson, priceLevel, tel, openHours, photoUrl, businessArea, sourceLabel, location, confidence, fitRationale)

data class ExploreFoodSpot(
    val id: String,
    val cityId: String,
    val name: String,
    val dish: String,
    val description: String,
    override val rating: String? = null,
    override val pricePerPerson: String? = null,
    override val priceLevel: String? = null,
    override val tel: String? = null,
    override val openHours: String? = null,
    override val photoUrl: String? = null,
    override val businessArea: String? = null,
    override val sourceLabel: String? = null,
    override val location: ExploreLocation? = null,
    override val confidence: String = "High",
    override val fitRationale: String = ""
) : ExploreRichMeta(rating, pricePerPerson, priceLevel, tel, openHours, photoUrl, businessArea, sourceLabel, location, confidence, fitRationale)

data class ExploreStay(
    val id: String,
    val cityId: String,
    val name: String,
    val area: String,
    val description: String,
    override val rating: String? = null,
    override val pricePerPerson: String? = null,
    override val priceLevel: String? = null,
    override val tel: String? = null,
    override val openHours: String? = null,
    override val photoUrl: String? = null,
    override val businessArea: String? = null,
    override val sourceLabel: String? = null,
    override val location: ExploreLocation? = null,
    override val confidence: String = "High",
    override val fitRationale: String = ""
) : ExploreRichMeta(rating, pricePerPerson, priceLevel, tel, openHours, photoUrl, businessArea, sourceLabel, location, confidence, fitRationale)

data class ExploreProviderStatus(
    val id: String,
    val label: String,
    val mode: String, // "static" | "live"
    val coverage: String,
    val candidates: List<String>,
    val nextIntegration: String,
    val limitations: List<String>
)

data class AmapPoi(
    val id: String,
    val name: String,
    val type: String,
    val address: JsonElement?,
    val adname: String?,
    @SerializedName("biz_type") val bizType: String?,
    val tel: JsonElement?,
    @SerializedName("opentime_week") val opentimeWeek: String?,
    @SerializedName("business_area") val businessArea: String?,
    val location: String?,
    @SerializedName("biz_ext") val bizExt: JsonElement?,
    val photos: List<AmapPoiPhoto>?
)

data class AmapPoiPhoto(
    val url: String?,
    val title: String?
)

data class ExploreAmapResponse(
    val ok: Boolean,
    val provider: String?,
    val city: String?,
    val category: String?,
    val pois: List<AmapPoi>?
)

data class ExploreAddToTripPayload(
    val id: String,
    val name: String,
    val cityId: String,
    val cityName: String,
    val category: String,
    val context: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val openingHours: String? = null,
    val mapUrl: String? = null,
    val sourceLabel: String? = null,
    val coordinates: ExploreLocation? = null,
    val bookingCandidates: List<space.go2china.visepanda.data.model.BookingCandidate>? = null
)
