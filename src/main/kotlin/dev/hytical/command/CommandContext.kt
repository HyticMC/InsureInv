package dev.hytical.command

import dev.hytical.HyticInv
import dev.hytical.economy.EconomyManager
import dev.hytical.managers.ConfigManager
import dev.hytical.messaging.MessageManager
import dev.hytical.storages.StorageManager
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

data class CommandContext(
    val sender: CommandSender,
    val args: Array<String>,
    val plugin: HyticInv,
    val configManager: ConfigManager,
    val storageManager: StorageManager,
    val economyManager: EconomyManager,
    val messageManager: MessageManager
) {
    val player: Player?
        get() = sender as? Player

    val playerOrThrow: Player
        get() = player ?: throw IllegalStateException("Sender is not a player")

    fun arg(index: Int): String? = args.getOrNull(index)

    fun argInt(index: Int): Int? = args.getOrNull(index)?.toIntOrNull()

    fun argDouble(index: Int): Double? = args.getOrNull(index)?.toDoubleOrNull()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CommandContext

        if (sender != other.sender) return false
        if (!args.contentEquals(other.args)) return false
        if (plugin != other.plugin) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sender.hashCode()
        result = 31 * result + args.contentHashCode()
        result = 31 * result + plugin.hashCode()
        return result
    }
}
