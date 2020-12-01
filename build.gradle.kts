import java.nio.charset.StandardCharsets

plugins {
    java
    `maven-publish`
    `kotlin-dsl`
    id("com.diffplug.spotless") version "5.8.2"
    id("fabric-loom") version "0.5-SNAPSHOT"
    id("org.jetbrains.kotlin.jvm") version "1.4.20"
}

val archivesBaseName: String by project
base.archivesBaseName = archivesBaseName

val mavenGroup: String by project
group = mavenGroup

val baseVersion: String by project
val minecraftVersion: String by project
version = "$baseVersion+$minecraftVersion"

val yarnBuild: String by project
val loaderVersion: String by project
val flkVersion: String by project

val fabricApiVersion: String by project

val distribution = "1.16.x"

repositories {
    mavenCentral()
    maven("https://jitpack.io") {
        name = "Jitpack"
    }
}

dependencies {
    // I believe we will need to use this more than once, so we made a configuration for it
    val modImplementationAndInclude by configurations.register("modImplementationAndInclude")

    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings("net.fabricmc:yarn:$minecraftVersion+build.$yarnBuild:v2")
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")
    modImplementation("net.fabricmc:fabric-language-kotlin:$flkVersion")
    modImplementationAndInclude("com.github.kotlin-graphics:openvr:v1.09c") {
        exclude(group = "net.java.dev.jna", module = "jna") // Use JNA Minecraft includes
    }

    add(sourceSets.main.get().getTaskName("mod", JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME), modImplementationAndInclude)
    add("include", modImplementationAndInclude)
}

// Long-term: Try to make it work on Java 8?
tasks.compileJava.configure {
    val targetVersion = 11

    if (JavaVersion.current().isJava12Compatible) {
        options.release.set(targetVersion)
    }
}

tasks.processResources {
    inputs.properties("version" to project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

java {
    withSourcesJar()
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesBaseName}"}
    }
}

spotless {
    java {
        // Only update license headers when changes have occurred
        ratchetFrom("origin/$distribution")
        licenseHeaderFile(project.file("HEADER")).yearSeparator(", ")
    }

    kotlin {
        // Only update license headers when changes have occurred
        ratchetFrom("origin/$distribution")
        licenseHeaderFile(project.file("HEADER")).yearSeparator(", ")
    }

    // Spotless tries to be smart by ignoring package-info files, however license headers are allowed there
    format("java-package-info") {
        target("**/package-info.java")

        // Only update license headers when changes have occurred
        ratchetFrom("origin/$distribution")

        // Regex is `/**` or `package`
        licenseHeaderFile(project.file("HEADER"), "/\\*\\*|package").yearSeparator(", ")
    }
}