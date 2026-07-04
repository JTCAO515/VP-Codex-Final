package space.go2china.visepanda.data.repository

import space.go2china.visepanda.data.explore.ExploreCategory
import space.go2china.visepanda.data.explore.ExplorePoi

interface ExploreRepository {
    suspend fun getPois(city: String, category: ExploreCategory): List<ExplorePoi>
    suspend fun isLiveMode(city: String): Boolean
}
