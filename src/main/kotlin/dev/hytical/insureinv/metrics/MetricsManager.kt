package dev.hytical.insureinv.metrics

import dev.hytical.insureinv.InsureInvPlugin
import dev.hytical.insureinv.metrics.detectors.EnvironmentDetector
import dev.hytical.insureinv.metrics.detectors.SystemInfo
import org.bstats.bukkit.Metrics
import org.bstats.charts.DrilldownPie
import org.bstats.charts.SimplePie
import org.bukkit.Bukkit
import java.util.concurrent.atomic.AtomicBoolean

class MetricsManager(
    private val plugin: InsureInvPlugin,
    private val pluginId: Int
) {
    private val started = AtomicBoolean(false)

    fun start() {
        if (!plugin.configManager.getMetrics()) {
            plugin.logger.info("bStats metrics disabled via config.")
            return
        }

        if (!started.compareAndSet(false, true)) return
        val metrics = Metrics(plugin, pluginId)

        registerServerEnvironmentChart(metrics)
        registerPluginVersionChart(metrics)
        registerSystemEnvironmentChart(metrics)
        registerOnlineModeChart(metrics)
        registerJavaVersionChart(metrics)
        registerPlayerCountChart(metrics)
        registerOSArchitectureChart(metrics)

        plugin.logger.info("bStats metrics enabled.")
    }

    private fun registerServerEnvironmentChart(metrics: Metrics) {
        metrics.addCustomChart(
            DrilldownPie("server_environment") {
                mapOf(
                    EnvironmentDetector.detect().name to
                            mapOf(Bukkit.getVersion() to 1)
                )
            }
        )
    }

    private fun registerPluginVersionChart(metrics: Metrics) {
        metrics.addCustomChart(
            SimplePie("plugin_version") {
                plugin.pluginMeta.version
            }
        )
    }

    private fun registerSystemEnvironmentChart(metrics: Metrics) {
        metrics.addCustomChart(
            SimplePie("system_environment") {
                SystemInfo.environmentInfo
            }
        )
    }

    private fun registerOnlineModeChart(metrics: Metrics) {
        metrics.addCustomChart(
            SimplePie("online_mode") {
                if (Bukkit.getOnlineMode()) "Online" else "Offline"
            }
        )
    }

    private fun registerJavaVersionChart(metrics: Metrics) {
        metrics.addCustomChart(
            DrilldownPie("java_information") {
                val vendor = System.getProperty("java.vendor")!!
                val version = System.getProperty("java.version")!!
                val bits = System.getProperty("sun.arch.data.models") ?: "Unknown"
                val runtime = System.getProperty("java.runtime.name")!!

                mapOf(
                    "Java Details" to mapOf(
                        "$runtime ($bits-bit)" to 1
                    ),
                    "Java Vendor" to mapOf(
                        vendor to 1
                    ),
                    "Java Version" to mapOf(
                        version to 1
                    )
                )
            }
        )
    }

    private fun registerPlayerCountChart(metrics: Metrics) {
        metrics.addCustomChart(
            SimplePie("player_count_range") {
                val onlineCount = Bukkit.getOnlinePlayers().size
                when {
                    onlineCount == 0 -> "0"
                    onlineCount <= 5 -> "1-5"
                    onlineCount <= 10 -> "6-10"
                    onlineCount <= 20 -> "11-20"
                    onlineCount <= 50 -> "21-50"
                    onlineCount <= 100 -> "51-100"
                    else -> "100+"
                }
            }
        )
    }

    private fun registerOSArchitectureChart(metrics: Metrics) {
        metrics.addCustomChart(
            SimplePie("os_architecture") {
                System.getProperty("os.arch")?.let { arch ->
                    if (arch.contains("64")) "64-bit" else "32-bit"
                } ?: "Unknown"
            }
        )
    }
}