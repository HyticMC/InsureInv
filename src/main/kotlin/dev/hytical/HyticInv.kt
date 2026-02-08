/*
 * HyticInv - A Minecraft inventory economy plugin
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

package dev.hytical

import dev.hytical.command.HyticInvCommand
import dev.hytical.economy.EconomyManager
import dev.hytical.listeners.PlayerDeath
import dev.hytical.managers.ConfigManager
import dev.hytical.managers.SchedulerManager
import dev.hytical.messaging.MessageManager
import dev.hytical.metrics.EnvironmentDetector
import dev.hytical.metrics.MetricsManager
import dev.hytical.metrics.ServerType
import dev.hytical.storages.StorageManager
import dev.hytical.utils.BuildInfo
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.plugin.java.JavaPlugin

class HyticInv : JavaPlugin() {
    private val pluginId: Int = 29351
    private var economyEnabled: Boolean = false

    lateinit var buildInfo: BuildInfo
        private set
    lateinit var configManager: ConfigManager
        private set
    lateinit var metricsManager: MetricsManager
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
        if (serverType == ServerType.SPIGOT || serverType == ServerType.UNKNOWN) {
            logger.severe("═══════════════════════════════════════════════════════════════")
            logger.severe("HyticInv requires Paper or Folia to run.")
            logger.severe("Spigot and other server software are not supported.")
            logger.severe("Please upgrade to Paper: https://papermc.io/downloads/paper")
            logger.severe("═══════════════════════════════════════════════════════════════")
            server.pluginManager.disablePlugin(this)
            return
        }

        buildInfo = BuildInfo(this)

        schedulerManager = SchedulerManager(this)

        if (schedulerManager.isFolia) {
            logger.info("Running on Folia - region-safe scheduling enabled")
        } else {
            logger.info("Running on Paper - standard scheduling enabled")
        }

        configManager = ConfigManager(this)

        metricsManager = MetricsManager(this, pluginId)
        metricsManager.start()

        messageManager = MessageManager(this, configManager)

        economyManager = EconomyManager(this)

        storageManager = StorageManager(this, configManager)
        if (!storageManager.initialize()) {
            logger.severe("Failed to initialize storage system! Disabling plugin...")
            server.pluginManager.disablePlugin(this)
            return
        }

        registerCommands()
        registerEvents()

        logger.info("HyticInv v${this.pluginMeta.version} enabled successfully!")
        sendStartupLog()
    }

    override fun onDisable() {
        if (::storageManager.isInitialized) {
            storageManager.shutdown()
        }

        if (::messageManager.isInitialized) {
            messageManager.shutdown()
        }

        logger.info("HyticInv disabled.")
    }

    private fun registerCommands() {
        val commandHandler = HyticInvCommand(
            this,
            configManager,
            storageManager,
            economyManager,
            messageManager
        )

        getCommand("hyticinv")?.apply {
            setExecutor(commandHandler)
            tabCompleter = commandHandler
        }
    }

    private fun registerEvents() {
        val playerDeath = PlayerDeath(
            this,
            configManager,
            storageManager,
            messageManager
        )

        server.pluginManager.registerEvents(playerDeath, this)
    }

    private fun sendStartupLog() {
        val log = listOf(
            "",
            " &bʜʏᴛɪᴄɪɴᴠ &7ᴠ${buildInfo.buildVersion}",
            " &8--------------------------------------",
            " &cɪɴꜰᴏʀᴍᴀᴛɪᴏɴ",
            "&7   • &fɴᴀᴍᴇ: &bʜʏᴛɪᴄɪɴᴠ",
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
