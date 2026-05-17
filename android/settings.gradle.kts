pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Pennyrush"

include(":app")
include(":core:common")
include(":core:database-cache")
include(":core:designsystem")
include(":core:network")
include(":core:security")
include(":feature:home")
include(":feature:transactions")
include(":feature:statements")
include(":feature:insights")
include(":feature:onboarding")
