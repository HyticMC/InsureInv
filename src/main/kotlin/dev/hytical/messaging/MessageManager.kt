package dev.arclyx.messaging

import dev.arclyx.HyticInv
import dev.arclyx.managers.ConfigManager
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class MessageManager(
    private val plugin: HyticInv,
    private val configManager: ConfigManager
) {
    private lateinit var audiences: BukkitAudiences
    private val miniMessage: MiniMessage = MiniMessage.builder().build()

    fun initialize() {
        audiences = BukkitAudiences.builder(plugin).build()
    }

    fun shutdown() {
        if (::audiences.isInitialized) {
            audiences.close()
        }
    }

    fun sendMessage(sender: CommandSender, messageKey: String, vararg resolvers: TagResolver) {
        val rawMessage = configManager.getMessage(messageKey)
        val component = parseMessage(rawMessage, *resolvers)
        audiences.sender(sender).sendMessage(component)
    }

    fun sendMessage(player: Player, messageKey: String, vararg resolvers: TagResolver) {
        val rawMessage = configManager.getMessage(messageKey)
        val component = parseMessage(rawMessage, *resolvers)
        audiences.player(player).sendMessage(component)
    }

    fun parseMessage(message: String, vararg resolvers: TagResolver): Component {
        return if (resolvers.isEmpty()) {
            miniMessage.deserialize(message)
        } else {
            miniMessage.deserialize(message, TagResolver.resolver(*resolvers))
        }
    }

    fun sendRawMessage(sender: CommandSender, message: String) {
        val component = miniMessage.deserialize(message)
        audiences.sender(sender).sendMessage(component)
    }
}