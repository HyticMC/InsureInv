package dev.hytical.command.subcommands

import dev.hytical.command.CommandContext
import dev.hytical.command.InsureInvSubCommand
import org.bukkit.command.CommandSender

class LangReloadSubCommand : InsureInvSubCommand {
    override val name = "langreload"
    override val permission = "insureinv.admin"
    override val requiresPlayer = false

    override fun execute(context: CommandContext) {
        context.plugin.reloadI18n()
        context.messageManager.sendMessage(context.sender, "system.lang-reload-complete")
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> = emptyList()
}
