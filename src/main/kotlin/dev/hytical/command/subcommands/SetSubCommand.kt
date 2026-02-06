package dev.hytical.command.subcommands

import dev.hytical.command.CommandContext
import dev.hytical.command.HyticSubCommand
import dev.hytical.utils.PlaceholderUtil
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class SetSubCommand : HyticSubCommand {
    override val name = "set"
    override val permission = "hyticinv.admin"
    override val requiresPlayer = false

    override fun execute(context: CommandContext) {
        val sender = context.sender
        val messageManager = context.messageManager
        val storageManager = context.storageManager

        val targetName = context.arg(1)
        if (targetName == null) {
            messageManager.sendMessage(sender, "usage-set")
            return
        }

        val targetPlayer = Bukkit.getPlayerExact(targetName)
        if (targetPlayer == null) {
            messageManager.sendMessage(
                sender, "player-not-found",
                PlaceholderUtil.of("player" to targetName)
            )
            return
        }

        val amount = context.argInt(2)
        if (amount == null || amount < 0) {
            if (context.arg(2) == null) {
                messageManager.sendMessage(sender, "usage-set")
            } else {
                messageManager.sendMessage(sender, "invalid-amount")
            }
            return
        }

        val playerData = storageManager.getPlayerData(targetPlayer)
        playerData.updateCharges(amount)
        storageManager.savePlayerData(playerData)

        messageManager.sendMessage(
            sender, "admin-set-success",
            PlaceholderUtil.of(
                "player" to targetPlayer.name,
                "amount" to amount.toString()
            )
        )
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return when (args.size) {
            2 -> {
                Bukkit.getOnlinePlayers()
                    .map { it.name }
                    .filter { it.lowercase().startsWith(args[1].lowercase()) }
            }

            3 -> listOf("0", "1", "5", "10").filter { it.startsWith(args[2]) }
            else -> emptyList()
        }
    }
}
