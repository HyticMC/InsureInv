package dev.hytical.command.subcommands

import dev.hytical.command.CommandContext
import dev.hytical.command.HyticSubCommand
import dev.hytical.utils.PlaceholderUtil
import org.bukkit.command.CommandSender

class SetPriceSubCommand : HyticSubCommand {
    override val name = "setprice"
    override val permission = "hyticinv.admin"
    override val requiresPlayer = false

    override fun execute(context: CommandContext) {
        val sender = context.sender
        val messageManager = context.messageManager
        val configManager = context.configManager

        val price = context.argDouble(1)
        if (price == null || price <= 0) {
            if (context.arg(1) == null) {
                messageManager.sendMessage(sender, "usage-setprice")
            } else {
                messageManager.sendMessage(sender, "invalid-amount")
            }
            return
        }

        configManager.setPricePerCharge(price)
        messageManager.sendMessage(
            sender, "price-updated",
            PlaceholderUtil.of("price" to String.format("%.2f", price))
        )
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return when (args.size) {
            2 -> listOf("100", "200", "500").filter { it.startsWith(args[1]) }
            else -> emptyList()
        }
    }
}
