package dev.hytical.metrics

object EnvironmentDetector {
    val type: ServerType by lazy { detectInternal() }

    fun detect(): ServerType = type

    private fun detectInternal(): ServerType = when {
        classExists("io.papermc.paper.threadedregions.RegionizedServer") -> ServerType.FOLIA
        classExists("com.destroystokyo.paper.PaperConfig") -> ServerType.PAPER
        classExists("org.spigotmc.SpigotConfig") -> ServerType.SPIGOT
        else -> ServerType.UNKNOWN
    }

    private fun classExists(name: String): Boolean =
        try {
            Class.forName(name)
            true
        } catch (_: ClassNotFoundException) {
            false
        }
}