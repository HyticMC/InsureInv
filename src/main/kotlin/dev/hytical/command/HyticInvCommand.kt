package dev.hytical.command

import dev.hytical.HyticInv
import dev.hytical.command.subcommands.*
import dev.hytical.economy.EconomyManager
import dev.hytical.managers.ConfigManager
import dev.hytical.messaging.MessageManager
import dev.hytical.storages.StorageManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class HyticInvCommand(
    private val plugin: HyticInv,
    private val configManager: ConfigManager,
    private val storageManager: StorageManager,
    private val economyManager: EconomyManager,
    private val messageManager: MessageManager
) : CommandExecutor, TabCompleter {

    private val subcommands: Map<String, HyticSubCommand>

    init {
        subcommands = buildMap {
            put("buy", BuySubCommand())
            put("toggle", ToggleSubCommand())
            put("info", InfoSubCommand())
            put("set", SetSubCommand())
            put("setprice", SetPriceSubCommand())
            put("setmax", SetMaxSubCommand())
            put("reload", ReloadSubCommand())
            put("help", HelpSubCommand { subcommands })
            put("version", HyticVersion(plugin))
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            executeSubcommand("help", sender, arrayOf("help"))
            return true
        }

        val subcommandName = args[0].lowercase()
        val subcommand = subcommands[subcommandName]

        if (subcommand == null) {
            executeSubcommand("help", sender, arrayOf("help"))
            return true
        }

        executeSubcommand(subcommandName, sender, args.map { it }.toTypedArray())
        return true
    }

    private fun executeSubcommand(name: String, sender: CommandSender, args: Array<String>) {
        val subcommand = subcommands[name] ?: return

        if (subcommand.permission != null && !sender.hasPermission(subcommand.permission!!)) {
            messageManager.sendMessage(sender, "no-permission")
            return
        }

        if (subcommand.requiresPlayer && sender !is Player) {
            messageManager.sendMessage(sender, "error-player-only")
            return
        }

        val context = CommandContext(
            sender = sender,
            args = args,
            plugin = plugin,
            configManager = configManager,
            storageManager = storageManager,
            economyManager = economyManager,
            messageManager = messageManager
        )

        subcommand.execute(context)
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        return when {
            args.size == 1 -> {
                subcommands.keys
                    .filter { it.startsWith(args[0].lowercase()) }
                    .filter { subcommandName ->
                        val subcommand = subcommands[subcommandName] ?: return@filter false
                        subcommand.permission == null || sender.hasPermission(subcommand.permission!!)
                    }
                    .sorted()
            }

            args.size > 1 -> {
                val subcommandName = args[0].lowercase()
                val subcommand = subcommands[subcommandName] ?: return emptyList()

                if (subcommand.permission != null && !sender.hasPermission(subcommand.permission!!)) {
                    return emptyList()
                }

                subcommand.tabComplete(sender, args.map { it }.toTypedArray())
            }

            else -> emptyList()
        }
    }

    fun getSubcommands(): Map<String, HyticSubCommand> = subcommands
}