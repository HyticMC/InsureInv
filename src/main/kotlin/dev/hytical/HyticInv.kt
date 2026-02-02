package dev.hytical

import dev.hytical.command.HyticInvCommand
import dev.hytical.command.HyticInvTabCompleter
import dev.hytical.listeners.PlayerDeath
import dev.hytical.managers.ConfigManager
import dev.hytical.managers.EconomyManager
import dev.hytical.messaging.MessageManager
import dev.hytical.storages.StorageManager
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
            logger.warning("Economy system unavailable. Purchase command will be disabled.")
        } else {
            econ = true
        }

        storageManager = StorageManager(this, configManager)
        if (!storageManager.initialize()) {
            logger.severe("Failed to initialize storage system! Disabling plugin...")
            server.pluginManager.disablePlugin(this)
            return
        }

        registerCommand()
    }

    override fun onDisable() {}

    private fun registerCommand() {
        val commandHandler = HyticInvCommand(
            this,
            configManager,
            storageManager,
            economyManager,
            messageManager
        )
        val tabCompleterHandler = HyticInvTabCompleter()

        getCommand("hyticinv")?.apply {
            setExecutor(commandHandler)
            tabCompleter = tabCompleterHandler
        }
    }

    private fun registerEvent() {
        val playerDeath = PlayerDeath(
            this,
            configManager,
            storageManager,
            messageManager
        )

        server.pluginManager.registerEvents(playerDeath, this)
    }
}
