pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolution {
    repositories {
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "VisePanda"

include(":app")
include(":core:designsystem")
include(":core:network")
include(":core:common")
include(":feature:home")
include(":feature:explore")
include(":feature:city")
include(":feature:chat")
include(":feature:trips")
include(":feature:tools")
include(":feature:auth")
