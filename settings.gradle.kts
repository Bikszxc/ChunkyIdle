// This project's structure is a modernized version of the (now archived)
// official https://github.com/stonecutter-versioning/stonecutter-template-architectury :
// Stonecutter handles the *Minecraft version* axis (it turns the root
// `common`/`fabric`/`neoforge` projects into one copy per targeted
// version under versions/<mcVersion>/<loader>/), while those three projects
// use the classic Architectury multiloader split for the *loader* axis.
// See README.md for the reasoning and for the alternative, non-Architectury
// https://github.com/stonecutter-versioning/stonecutter-template-multiloader
// approach this project deliberately did not take.
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.architectury.dev/") { name = "Architectury" }
        maven("https://maven.neoforged.net/releases/") { name = "NeoForge" }
        maven("https://maven.kikugie.dev/releases") { name = "Stonecutter" }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.7"
    // 1.20.1 needs Java 17, 26.2 needs Java 25 - none of them may be locally
    // installed, so let Gradle download whichever JDK each version needs.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "chunkyidle"

stonecutter {
    centralScript = "build.gradle.kts"
    kotlinController = true

    create(rootProject) {
        // Build every Fabric version in Chunky's published compatibility ranges. Loader
        // branches below remain limited to the releases they currently support.
        versions("1.20", "1.20.1", "1.20.2", "1.20.3", "1.20.4", "1.20.5", "1.20.6")
        versions("1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11")
        versions("26.1", "26.1.1", "26.1.2", "26.2", "26.3").buildscript("build.26.gradle.kts")

        branch("fabric") {
            inherit()
            versions("26.1", "26.1.1", "26.1.2", "26.2", "26.3").buildscript("build.26.gradle.kts")
        }

        branch("neoforge") {
            versions("1.20.2", "1.20.4")
            versions("1.21.1", "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11")
            versions("26.1", "26.1.1", "26.1.2", "26.2", "26.3").buildscript("build.26.gradle.kts")
        }

        vcsVersion = "1.20.1"
    }
}
