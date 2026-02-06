package dev.hytical

import dev.hytical.command.HyticInvCommand
import dev.hytical.listeners.PlayerDeath
import dev.hytical.managers.ConfigManager
import dev.hytical.managers.EconomyManager
import dev.hytical.managers.SchedulerManager
import dev.hytical.messaging.MessageManager
import dev.hytical.metrics.MetricsManager
import dev.hytical.storages.StorageManager
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.plugin.java.JavaPlugin

class HyticInv : JavaPlugin() {
    private val pluginId: Int = 29351
    private var economyEnabled: Boolean = false

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
        if (!economyManager.initialize()) {
            economyEnabled = false
            logger.warning("Economy system unavailable. Purchase command will be disabled.")
        } else {
            economyEnabled = true
        }

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
            " &bʜʏᴛɪᴄɪɴᴠ &7ᴠ${pluginMeta.version}",
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

    fun isEconomyEnabled(): Boolean = economyEnabled
}
