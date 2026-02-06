package dev.hytical.metrics

import dev.hytical.HyticInv
import org.bstats.bukkit.Metrics
import org.bstats.charts.DrilldownPie
import org.bstats.charts.SimplePie
import org.bukkit.Bukkit
import java.util.concurrent.atomic.AtomicBoolean

class MetricsManager(
    private val plugin: HyticInv,
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
                SystemInformer.environmentInfo
            }
        )
    }
}