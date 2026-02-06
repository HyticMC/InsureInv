package dev.hytical.messaging

import dev.hytical.HyticInv
import dev.hytical.managers.ConfigManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class MessageManager(
    private val plugin: HyticInv,
    private val configManager: ConfigManager
) {
    private val miniMessage: MiniMessage = MiniMessage.miniMessage()

    private val noPrefixKeys = setOf(
        "help-header", "help-footer", "help-buy", "help-toggle", "help-info",
        "help-set", "help-setprice", "help-setmax", "help-reload", "help-help",
        "info-header", "info-footer", "status-enabled", "status-disabled"
    )

    private val placeholderPattern = Regex("\\{([^}]+)}")

    fun shutdown() {}

    fun sendMessage(sender: CommandSender, messageKey: String, placeholders: Map<String, String> = emptyMap()) {
        val rawMessage = configManager.getMessage(messageKey)
        val shouldAddPrefix = configManager.isPrefixEnabled() && sender is Player && !noPrefixKeys.contains(messageKey)
        val fullMessage = if (shouldAddPrefix) configManager.getPrefix() + rawMessage else rawMessage
        val resolvedMessage = replacePlaceholders(fullMessage, placeholders)
        val component = parseMessage(resolvedMessage)
        sender.sendMessage(component)
    }

    fun sendMessage(player: Player, messageKey: String, placeholders: Map<String, String> = emptyMap()) {
        val rawMessage = configManager.getMessage(messageKey)
        val shouldAddPrefix = configManager.isPrefixEnabled() && !noPrefixKeys.contains(messageKey)
        val fullMessage = if (shouldAddPrefix) configManager.getPrefix() + rawMessage else rawMessage
        val resolvedMessage = replacePlaceholders(fullMessage, placeholders)
        val component = parseMessage(resolvedMessage)
        player.sendMessage(component)
    }

    private fun replacePlaceholders(message: String, placeholders: Map<String, String>): String {
        if (placeholders.isEmpty()) return message

        return placeholderPattern.replace(message) { matchResult ->
            val key = matchResult.groupValues[1]
            placeholders[key] ?: matchResult.value
        }
    }

    fun parseMessage(message: String): Component {
        return miniMessage.deserialize(message)
    }

    fun sendRawMessage(sender: CommandSender, message: String, placeholders: Map<String, String> = emptyMap()) {
        val resolvedMessage = replacePlaceholders(message, placeholders)
        val component = parseMessage(resolvedMessage)
        sender.sendMessage(component)
    }
}