package dev.hytical.insureinv.command.subcommands

import dev.hytical.insureinv.command.CommandContext
import dev.hytical.insureinv.command.SubCommand
import dev.hytical.insureinv.utils.PlaceholderUtil
import org.bukkit.command.CommandSender

class HelpCommand(
    private val getSubcommands: () -> Map<String, SubCommand>
) : SubCommand {
    override val name = "help"
    override val permission: String? = null
    override val requiresPlayer = false

    override fun execute(context: CommandContext) {
        val sender = context.sender
        val messageManager = context.messageManager

        val page = context.argInt(1) ?: 1
        sendHelp(sender, page, context)
    }

    private fun sendHelp(sender: CommandSender, page: Int, context: CommandContext) {
        val messageManager = context.messageManager

        val helpCommands = mutableListOf<String>()

        helpCommands.add("help-buy")
        helpCommands.add("help-toggle")
        helpCommands.add("help-info")

        if (sender.hasPermission("hyticinv.admin")) {
            helpCommands.add("help-set")
            helpCommands.add("help-setprice")
            helpCommands.add("help-setmax")
            helpCommands.add("help-reload")
        }

        helpCommands.add("help-help")

        val commandsPerPage = 5
        val totalPages = (helpCommands.size + commandsPerPage - 1) / commandsPerPage
        val actualPage = page.coerceIn(1, totalPages)

        val startIndex = (actualPage - 1) * commandsPerPage
        val endIndex = (startIndex + commandsPerPage).coerceAtMost(helpCommands.size)

        messageManager.sendMessage(
            sender, "help-header",
            PlaceholderUtil.pagination(actualPage, totalPages)
        )

        for (i in startIndex until endIndex) {
            messageManager.sendMessage(sender, helpCommands[i])
        }

        messageManager.sendMessage(sender, "help-footer")
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return when (args.size) {
            2 -> listOf("1", "2").filter { it.startsWith(args[1]) }
            else -> emptyList()
        }
    }
}
