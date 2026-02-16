plugins {
    kotlin("jvm")
    id("com.gradleup.shadow")
}

// ---------------------------------------------------------------------------
// Git commit hash
// ---------------------------------------------------------------------------
val gitCommitShort: String = providers.exec {
    commandLine("git", "rev-parse", "--short", "HEAD")
    isIgnoreExitValue = true
}.standardOutput.asText.map { it.trim() }.getOrElse("unknown")

// ---------------------------------------------------------------------------
// Repositories
// ---------------------------------------------------------------------------
repositories {
    maven { url = uri("libs") }                                              // local hyticallib-i18n jars
    maven("https://repo.papermc.io/repository/maven-public/")                // Paper API
    maven("https://jitpack.io")                                              // VaultAPI
    maven("https://repo.helpch.at/releases")                                 // PlaceholderAPI
    maven("https://repo.tcoded.com/releases")                                // FoliaLib
    maven("https://repo.rosewooddev.io/repository/public/")                  // PlayerPoints
    mavenCentral()
}

// ---------------------------------------------------------------------------
// Dependencies
// ---------------------------------------------------------------------------
val coroutinesVersion: String by project

dependencies {
    // --- provided by server / other plugins → compileOnly ---
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("org.black_ixx:playerpoints:3.3.3")
    compileOnly("com.zaxxer:HikariCP:5.1.0")
    compileOnly("com.mysql:mysql-connector-j:8.3.0")
    compileOnly("org.xerial:sqlite-jdbc:3.45.1.0")
    compileOnly("com.google.code.gson:gson:2.10.1")

    // --- loaded via Paper Library Loader → compileOnly ---
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    // --- shaded into JAR → implementation ---
    implementation("org.bstats:bstats-bukkit:3.1.0")
    implementation("com.tcoded:FoliaLib:0.4.3")
    implementation("dev.hytical:hyticallib-i18n-core:1.0.0")
    implementation("dev.hytical:hyticallib-i18n-bukkit:1.0.0")
}

// ---------------------------------------------------------------------------
// Kotlin
// ---------------------------------------------------------------------------
kotlin {
    jvmToolchain(21)
}

// ---------------------------------------------------------------------------
// Resource filtering  (plugin.yml token replacement)
// ---------------------------------------------------------------------------
tasks.processResources {
    val props = mapOf(
        "name"      to project.name,
        "version"   to project.version.toString(),
        "gitCommit" to gitCommitShort,
    )
    inputs.properties(props)
    filesMatching("plugin.yml") {
        expand(props)
    }
}

// ---------------------------------------------------------------------------
// Shadow JAR
// ---------------------------------------------------------------------------
tasks.shadowJar {
    archiveClassifier.set("")
    archiveFileName.set("${project.name}-${project.version}+${gitCommitShort}.jar")

    dependencies {
        include(dependency("com.tcoded:FoliaLib"))
        include(dependency("org.bstats:bstats-bukkit"))
        include(dependency("org.bstats:bstats-base"))
        include(dependency("dev.hytical:hyticallib-i18n-core"))
        include(dependency("dev.hytical:hyticallib-i18n-bukkit"))
    }

    relocate("com.tcoded.folialib", "dev.hytical.libs.folialib")
    relocate("org.bstats", "dev.hytical.libs.bstats")

    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
    exclude("META-INF/MANIFEST.MF")
    exclude("META-INF/LICENSE*")
    exclude("META-INF/NOTICE*")
    exclude("META-INF/versions/**")
    exclude("META-INF/maven/**")
    exclude("META-INF/proguard/**")
}

// shadowJar replaces the default jar
tasks.build {
    dependsOn(tasks.shadowJar)
}
