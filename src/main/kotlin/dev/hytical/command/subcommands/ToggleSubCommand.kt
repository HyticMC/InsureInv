package dev.hytical.command.subcommands

import dev.hytical.command.CommandContext
import dev.hytical.command.HyticSubCommand
import dev.hytical.utils.PlaceholderUtil
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class ToggleSubCommand : HyticSubCommand {
    override val name = "toggle"
    override val permission: String? = null
    override val requiresPlayer = false

    override fun execute(context: CommandContext) {
        val sender = context.sender
        val messageManager = context.messageManager
        val storageManager = context.storageManager
        val configManager = context.configManager

        val targetName = context.arg(1)

        if (targetName != null) {
            if (!sender.hasPermission("hyticinv.admin")) {
                messageManager.sendMessage(sender, "no-permission")
                return
            }

            val targetPlayer = Bukkit.getPlayerExact(targetName) ?: run {
                messageManager.sendMessage(
                    sender, "player-not-found",
                    PlaceholderUtil.of("player" to targetName)
                )
                return
            }

            val playerData = storageManager.getPlayerData(targetPlayer)
            playerData.protectionEnabled = !playerData.protectionEnabled
            storageManager.savePlayerData(playerData)

            val messageKey = if (playerData.protectionEnabled) "toggle-on" else "toggle-off"
            messageManager.sendMessage(
                targetPlayer, messageKey,
                PlaceholderUtil.charges(playerData.charges, configManager.getMaxCharges())
            )

            if (sender != targetPlayer) {
                messageManager.sendMessage(
                    sender, "admin-toggle-success",
                    PlaceholderUtil.of(
                        "status" to if (playerData.protectionEnabled) "on" else "off",
                        "player" to targetPlayer.name
                    )
                )
            }
        } else {
            val player = context.player
            if (player == null) {
                messageManager.sendMessage(sender, "error-player-only")
                return
            }

            if (!sender.hasPermission("hyticinv.use")) {
                messageManager.sendMessage(sender, "no-permission")
                return
            }

            val playerData = storageManager.getPlayerData(player)
            playerData.protectionEnabled = !playerData.protectionEnabled
            storageManager.savePlayerData(playerData)

            val messageKey = if (playerData.protectionEnabled) "toggle-on" else "toggle-off"
            messageManager.sendMessage(
                player, messageKey,
                PlaceholderUtil.charges(playerData.charges, configManager.getMaxCharges())
            )
        }
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return when (args.size) {
            2 -> {
                if (sender.hasPermission("hyticinv.admin")) {
                    Bukkit.getOnlinePlayers()
                        .map { it.name }
                        .filter { it.lowercase().startsWith(args[1].lowercase()) }
                } else {
                    emptyList()
                }
            }

            else -> emptyList()
        }
    }
}
