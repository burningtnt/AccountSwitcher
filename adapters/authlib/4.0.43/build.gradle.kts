plugins {
    java
}

version = rootProject.version
val authlib = "4.0.43"

repositories {
    maven(url = "https://repo.glaremasters.me/repository/public/")
}

dependencies {
    compileOnly("com.mojang:authlib:${authlib}")
    compileOnly("com.google.guava:guava:31.1-jre")
    compileOnly("com.google.code.gson:gson:2.10.1")
    compileOnly("org.slf4j:slf4j-api:2.0.1")
    implementation(rootProject)
}

tasks.processResources {
    mapOf(
        "version" to project.version,
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