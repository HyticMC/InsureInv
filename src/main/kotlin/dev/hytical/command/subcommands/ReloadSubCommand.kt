package dev.hytical.command.subcommands

import dev.hytical.command.CommandContext
import dev.hytical.command.HyticSubCommand
import dev.hytical.utils.PlaceholderUtil
import org.bukkit.command.CommandSender

class ReloadSubCommand : HyticSubCommand {
    override val name = "reload"
    override val permission = "hyticinv.admin"
    override val requiresPlayer = false

    override fun execute(context: CommandContext) {
        val sender = context.sender
        val messageManager = context.messageManager
        val configManager = context.configManager
        val storageManager = context.storageManager

        val oldBackend = storageManager.getCurrentBackendName()
        configManager.reload()
        storageManager.reload()

        val newBackend = storageManager.getCurrentBackendName()

        messageManager.sendMessage(sender, "reload-complete")

        if (oldBackend != newBackend) {
            messageManager.sendMessage(
                sender, "reload-storage-changed",
                PlaceholderUtil.method(newBackend)
            )
        }
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return emptyList()
    }
}
