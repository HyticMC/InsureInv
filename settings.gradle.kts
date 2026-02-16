pluginManagement {
    val kotlinVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        id("com.gradleup.shadow") version "9.0.0-beta12"
    }
}

rootProject.name = "InsureInv"
