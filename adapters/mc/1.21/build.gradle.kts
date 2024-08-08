plugins {
    id("fabric-loom") version "1.6-SNAPSHOT"
}

version = rootProject.version

val minecraft = "1.21"
val yarn = 9
val loader = "0.15.11"
val authlib = "6.0.54"

dependencies {
    minecraft("com.mojang:minecraft:$minecraft")
    mappings("net.fabricmc:yarn:$minecraft+build.${yarn}:v2")
    modImplementation("net.fabricmc:fabric-loader:${loader}")
    modRuntimeOnly("net.fabricmc.fabric-api:fabric-resource-loader-v0:1.1.5+78017270d1")
    modRuntimeOnly("io.github.llamalad7:mixinextras-fabric:0.3.5")

    implementation(project(":adapters:authlib:${authlib}"))
    implementation(rootProject)
}

tasks.processResources {
    mapOf(
        "version" to project.version,
        "loader" to loader,
        "minecraft" to minecraft,
        "authlib" to authlib
    ).let { map ->
        inputs.properties(map)

        filesMatching("fabric.mod.json") {
            expand(map)
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    sourceCompatibility = "17"
    targetCompatibility = "17"
}