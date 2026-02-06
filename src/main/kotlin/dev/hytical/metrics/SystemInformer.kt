package dev.hytical.metrics

import java.io.File

object SystemInformer {

    val environmentInfo: String by lazy { fetchEnvironment() }

    private fun fetchEnvironment(): String {
        val osName = System.getProperty("os.name")

        if (!osName.contains("Linux", ignoreCase = true)) {
            return osName
        }

        val distro = getLinuxDistro()

        val isDocker = File("/.dockerenv").exists() ||
                File("/proc/self/cgroup").useLines { lines -> lines.any { it.contains("docker") } }
        val isPterodactyl = System.getenv("P_SERVER_UUID") != null

        return when {
            isPterodactyl -> "Pterodactyl ($distro)"
            isDocker -> "Docker ($distro)"
            else -> "Linux ($distro)"
        }
    }

    private fun getLinuxDistro(): String {
        val osRelease = File("/etc/os-release")
        if (!osRelease.exists()) return "Unknown Linux"

        return try {
            val props = osRelease.readLines().associate { line ->
                val parts = line.split("=", limit = 2)
                if (parts.size == 2) {
                    parts[0] to parts[1].replace("\"", "")
                } else "" to ""
            }
            props["PRETTY_NAME"] ?: props["ID"] ?: "Unknown Linux"
        } catch (e: Exception) {
            "Linux"
        }
    }
}