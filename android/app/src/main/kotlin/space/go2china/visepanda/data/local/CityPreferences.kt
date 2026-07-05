package space.go2china.visepanda.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Persists the last-selected Explore city across sessions. */
@Singleton
class CityPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("visepanda_city_prefs", Context.MODE_PRIVATE)

    fun getSelectedCityId(): String? = prefs.getString("selected_city_id", null)

    fun saveSelectedCityId(cityId: String) {
        prefs.edit().putString("selected_city_id", cityId).apply()
    }
}
