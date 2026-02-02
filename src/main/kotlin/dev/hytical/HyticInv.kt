package dev.arclyx

import dev.arclyx.managers.ConfigManager
import dev.arclyx.managers.EconomyManager
import dev.arclyx.messaging.MessageManager
import dev.arclyx.storages.StorageManager
import org.bukkit.plugin.java.JavaPlugin

class HyticInv : JavaPlugin() {
    private var econ: Boolean = false

    private lateinit var configManager: ConfigManager
    private lateinit var messageManager: MessageManager
    private lateinit var economyManager: EconomyManager
    private lateinit var storageManager: StorageManager

    override fun onEnable() {
        configManager = ConfigManager(this)

        messageManager = MessageManager(this, configManager)
        messageManager.initialize()

        economyManager = EconomyManager(this)
        if (!economyManager.initialize()) {
            econ = false
            logger.warning("Economy system unavailable. Purchase commands will be disabled.")
        } else {
            econ = true
        }

        storageManager = StorageManager(this, configManager)
        if (!storageManager.initialize()) {
            logger.severe("Failed to initialize storage system! Disabling plugin...")
            server.pluginManager.disablePlugin(this)
            return
        }
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
