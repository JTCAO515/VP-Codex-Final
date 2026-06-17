package space.jtcao.visepanda.feature.navigation

object RewriteRoutes {
    const val HOME = "home"
    const val EXPLORE = "explore"
    const val CHAT = "chat"
    const val CHAT_WITH_CITY = "chat/{cityId}"
    const val TRIPS = "trips"
    const val TOOLS = "tools"
    const val DESTINATION = "destination/{cityId}"
    const val ACCOUNT = "account"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot-password"
    const val RESET_PASSWORD = "reset-password"

    fun chat(cityId: String): String = "chat/$cityId"
    fun destination(cityId: String): String = "destination/$cityId"
}
