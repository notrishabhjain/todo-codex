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

rootProject.name = "OfflineTaskManager"

include(
    ":app",
    ":core",
    ":domain",
    ":data",
    ":ml",
    ":feature-tasks",
    ":feature-inbox",
    ":feature-analytics",
    ":feature-settings",
    ":feature-transcripts",
)
