import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import kotlin.io.path.bufferedReader
import kotlin.io.path.bufferedWriter
import kotlin.io.path.copyTo

plugins {
    java
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.google.code.gson:gson:2.10.1")
    }
}

version = "1.1.0"

repositories {
    maven(url = "https://maven.fabricmc.net/")
    mavenCentral()
}

dependencies {
    compileOnly("net.fabricmc:fabric-loader:0.15.11")
    compileOnly("com.google.code.gson:gson:2.10.1")
    compileOnly("com.google.guava:guava:31.1-jre")
    compileOnly("org.apache.httpcomponents:httpclient:4.5.13")
    compileOnly("org.apache.logging.log4j:log4j-api:2.22.1")
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version
        )
    }
}

data class Adapter(val project: String, val builder: String, val prefix: String)

val universal = tasks.create("universal") {
    group = "build"

    val adapters = mapOf(
        listOf(
            "adapters:authlib:4.0.43",
            "adapters:authlib:6.0.52",
            "adapters:authlib:6.0.54"
        ) to Pair("jar", "authlib"),
        listOf(
            "adapters:mc:1.20.1",
            "adapters:mc:1.20.4",
            "adapters:mc:1.20.6",
            "adapters:mc:1.21"
        ) to Pair("remapJar", "mc")
    ).flatMap { (projects, value) ->
        projects.map { project ->
            Adapter(project, value.first, value.second)
        }
    }

    adapters.forEach { adapter -> dependsOn(":${adapter.project}:${adapter.builder}") }

    outputs.upToDateWhen {
        adapters.all { adapter -> project(adapter.project).tasks.getByName(adapter.builder).state.upToDate }
    }

    val output = project.layout.buildDirectory.file("libs/${project.name}-${project.version}-universal.jar");
    outputs.file(output)

    doLast {
        val outputFile = output.get().asFile

        outputFile.delete()
        project.layout.buildDirectory.file("libs/${project.name}-${project.version}.jar")
            .get().asFile.copyTo(outputFile)

        FileSystems.newFileSystem(
            URI.create("jar:" + outputFile.toURI()), emptyMap<String, Any>()
        ).use { fs ->
            Files.createDirectories(fs.getPath("/META-INF/jars"))

            val e = fs.getPath("/fabric.mod.json").bufferedReader().use {
                Gson().fromJson(it, JsonElement::class.java)
            } as JsonObject

            val jars = JsonArray().also {
                e.add("jars", it)
            }

            adapters.forEach { adapter ->
                val p = project(adapter.project)
                val fileName = "${p.name}-${p.version}.jar"
                p.layout.buildDirectory.file("libs/$fileName").get().asFile.toPath().copyTo(
                    fs.getPath("/META-INF/jars/adapter-${adapter.prefix}-$fileName")
                )

                JsonObject().also {
                    it.addProperty("file", "META-INF/jars/adapter-${adapter.prefix}-$fileName")

                    jars.add(it)
                }
            }

            fs.getPath("/fabric.mod.json").bufferedWriter().use {
                GsonBuilder().setPrettyPrinting().create().toJson(e, it)
            }
        }
    }
}

tasks.build {
    dependsOn(universal)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    sourceCompatibility = "17"
    targetCompatibility = "17"
}