package dev.hytical.command

import org.bukkit.command.CommandSender

interface HyticSubCommand {
    val name: String
    val permission: String?
    val requiresPlayer: Boolean

    fun execute(context: CommandContext)
    fun tabComplete(sender: CommandSender, args: Array<String>): List<String>
}
