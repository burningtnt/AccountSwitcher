pluginManagement {
    repositories {
        maven(url = "https://maven.fabricmc.net/")
        gradlePluginPortal()
    }
}

rootProject.name = "AccountsX"

val adapters = listOf(
    "adapters:authlib:4.0.43",
    "adapters:authlib:6.0.52",
    "adapters:authlib:6.0.54",
    "adapters:mc:1.20.1",
    "adapters:mc:1.20.4",
    "adapters:mc:1.20.6",
    "adapters:mc:1.21"
)

include(adapters)