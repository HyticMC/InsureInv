package dev.hytical.listeners

import dev.hytical.HyticInv
import dev.hytical.managers.ConfigManager
import dev.hytical.messaging.MessageManager
import dev.hytical.storages.StorageManager
import dev.hytical.utils.PlaceholderUtil
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class PlayerDeath(
    private val plugin: HyticInv,
    private val configManager: ConfigManager,
    private val storageManager: StorageManager,
    private val messageManager: MessageManager
) : Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity
        val playerData = storageManager.getPlayerData(player)

        if (!playerData.protectionEnabled) {
            messageManager.sendMessage(player, "protection-disabled")
            return
        }

        if (!playerData.hasCharges()) {
            messageManager.sendMessage(player, "no-charges")
            return
        }

        playerData.consumeCharge()
        storageManager.savePlayerData(playerData, async = true)

        event.keepInventory = true
        event.keepLevel = true
        event.drops.clear()
        event.droppedExp = 0

        messageManager.sendMessage(
            player, "protection-triggered",
            PlaceholderUtil.charges(playerData.charges, configManager.getMaxCharges())
        )
    }
}