/*
 * InsureInvPlugin - A Minecraft inventory economy plugin
 * Copyright (C) 2024  HyticMC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.hytical.insureinv

import dev.hytical.insureinv.command.InsureInvCommand
import dev.hytical.insureinv.economy.EconomyManager
import dev.hytical.insureinv.i18n.I18nManager
import dev.hytical.insureinv.i18n.MessageManager
import dev.hytical.insureinv.i18n.PluginLanguage
import dev.hytical.insureinv.listeners.PlayerDeathListener
import dev.hytical.insureinv.listeners.PlayerLocateListener
import dev.hytical.insureinv.managers.ConfigManager
import dev.hytical.insureinv.managers.SchedulerManager
import dev.hytical.insureinv.metrics.MetricsManager
import dev.hytical.insureinv.metrics.ServerPlatform
import dev.hytical.insureinv.metrics.detectors.EnvironmentDetector
import dev.hytical.insureinv.storages.StorageManager
import dev.hytical.insureinv.utils.PluginBuildInfo
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.plugin.java.JavaPlugin

open class InsureInvPlugin : JavaPlugin() {
    private val pluginId: Int = 29351

    lateinit var pluginBuildInfo: PluginBuildInfo
        private set
    lateinit var configManager: ConfigManager
        private set
    lateinit var metricsManager: MetricsManager
        private set
    lateinit var i18nManager: I18nManager
        private set
    lateinit var messageManager: MessageManager
        private set
    lateinit var economyManager: EconomyManager
        private set
    lateinit var storageManager: StorageManager
        private set
    lateinit var schedulerManager: SchedulerManager
        private set

    override fun onEnable() {
        val serverType = EnvironmentDetector.detect()
        if (serverType == ServerPlatform.SPIGOT || serverType == ServerPlatform.UNKNOWN) {
            logger.severe("═══════════════════════════════════════════════════════════════")
            logger.severe("InsureInvPlugin requires Paper or Folia to run.")
            logger.severe("Spigot and other server software are not supported.")
            logger.severe("Please upgrade to Paper: https://papermc.io/downloads/paper")
            logger.severe("═══════════════════════════════════════════════════════════════")
            server.pluginManager.disablePlugin(this)
            return
        }

        pluginBuildInfo = PluginBuildInfo(this)

        schedulerManager = SchedulerManager(this)

        if (schedulerManager.isFolia) {
            logger.info("Running on Folia - region-safe scheduling enabled")
        } else {
            logger.info("Running on Paper - standard scheduling enabled")
        }

        configManager = ConfigManager(this)

        metricsManager = MetricsManager(this, pluginId)
        metricsManager.start()

        i18nManager = I18nManager(this, configManager.getDefaultLanguage())
        i18nManager.initialize()
        PluginLanguage.bind(i18nManager)

        messageManager = MessageManager(this, configManager)

        economyManager = EconomyManager(this)
        economyManager.initialize()

        storageManager = StorageManager(this, configManager)
        if (!storageManager.initialize()) {
            logger.severe("Failed to initialize storage system! Disabling plugin...")
            server.pluginManager.disablePlugin(this)
            return
        }

        registerCommands()
        registerEvents()

        logger.info("InsureInvPlugin v${this.pluginMeta.version} enabled successfully! Have Fun :D")
        sendStartupLog()
    }

    override fun onDisable() {
        if (::i18nManager.isInitialized) {
            i18nManager.shutdown()
        }

        if (::storageManager.isInitialized) {
            storageManager.shutdown()
        }

        logger.info("InsureInvPlugin disabled.")
    }

    fun reloadI18n() {
        i18nManager.rebuild()
    }

    private fun registerCommands() {
        val commandHandler = InsureInvCommand(
            this,
            configManager,
            storageManager,
            economyManager,
            messageManager
        )

        getCommand("insureinv")?.apply {
            setExecutor(commandHandler)
            tabCompleter = commandHandler
        }
    }

    private fun registerEvents() {
        val playerDeathListener = PlayerDeathListener(
            configManager,
            storageManager,
            messageManager
        )

        val playerLocateListener = PlayerLocateListener(
            i18nManager,
            configManager
        )

        server.pluginManager.registerEvents(playerDeathListener, this)
        server.pluginManager.registerEvents(playerLocateListener, this)
    }

    private fun sendStartupLog() {
        listOf(
            "",
            " &b${pluginBuildInfo.getPluginName(true)} &7ᴠ${pluginBuildInfo.buildVersion}",
            " &8--------------------------------------",
            " &cɪɴꜰᴏʀᴍᴀᴛɪᴏɴ",
            "&7   • &fɴᴀᴍᴇ: &b${pluginBuildInfo.getPluginName(true)}",
            "&7   • &fᴀᴜᴛʜᴏʀ: &bʜʏᴛɪᴄᴍᴄ",
            " &8--------------------------------------",
            ""
        ).forEach {
            server.consoleSender.sendMessage(
                LegacyComponentSerializer.legacyAmpersand().deserialize(it)
            )
        }
    }
}
