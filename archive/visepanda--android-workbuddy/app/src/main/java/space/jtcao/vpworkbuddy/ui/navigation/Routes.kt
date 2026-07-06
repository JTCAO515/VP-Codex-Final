package space.jtcao.vpworkbuddy.ui.navigation

/**
 * VisePanda Navigation Routes
 *
 * Matches the 5-tab structure from the web version:
 *   Home · Chat · Map · Trips · Cities · Tools
 *
 * Routes without arguments that will be added later as screens are implemented.
 */
object Routes {
    const val HOME = "home"
    const val CHAT = "chat"
    const val CHAT_CITY = "chat/{city}"
    const val MAP = "map"
    const val TRIPS = "trips"
    const val CITIES = "cities"
    const val CITY_DETAIL = "cities/{cityName}"
    const val TOOLS = "tools"
    const val TOOL_DETAIL = "tools/{toolName}"

    fun chatCity(city: String) = "chat/$city"
    fun cityDetail(cityName: String) = "cities/$cityName"
    fun toolDetail(toolName: String) = "tools/$toolName"
}
