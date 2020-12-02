import org.gradle.internal.os.OperatingSystem

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
}

val lwjglVersion = "3.2.2"
val lwjglNatives = "natives-windows"

dependencies {
    // I believe we will need to use this more than once, so we made a configuration for it
    val implementationAndInclude by configurations.register("implementationAndInclude")

    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings("net.fabricmc:yarn:$minecraftVersion+build.$yarnBuild:v2")
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")
    modImplementation("net.fabricmc:fabric-language-kotlin:$flkVersion")

    // Match MC's LWJGL version
    implementation("org.lwjgl", "lwjgl", lwjglVersion)
    implementation("org.lwjgl", "lwjgl-openvr", lwjglVersion)
    runtimeOnly("org.lwjgl", "lwjgl", lwjglVersion, classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-openvr", lwjglVersion, classifier = lwjglNatives)

    add(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME, implementationAndInclude)
    add("include", implementationAndInclude)
}

tasks.compileJava.configure {
    val targetVersion = 8

    if (JavaVersion.current().isJava9Compatible) {
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

tasks.compileKotlin {
    kotlinOptions {
        jvmTarget = "8"
    }
}
