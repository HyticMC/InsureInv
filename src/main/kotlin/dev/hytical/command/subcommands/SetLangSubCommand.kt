package dev.hytical.command.subcommands

import dev.hytical.command.CommandContext
import dev.hytical.command.InsureInvSubCommand
import dev.hytical.i18n.PluginLang
import org.bukkit.command.CommandSender

class SetLangSubCommand : InsureInvSubCommand {
    override val name = "setlang"
    override val permission: String? = null
    override val requiresPlayer = true

    override fun execute(context: CommandContext) {
        val player = context.playerOrThrow
        val i18nManager = context.plugin.i18nManager
        val langCode = context.arg(1)

        if (langCode == null) {
            val currentLang = i18nManager.service.getLanguage(player.uniqueId)
            val available = i18nManager.registry.languages.joinToString(", ")
            PluginLang.msg(
                player,
                "system.lang-current",
                mapOf("lang" to currentLang, "available" to available)
            )
            return
        }

        if (!i18nManager.registry.languages.contains(langCode)) {
            val available = i18nManager.registry.languages.joinToString(", ")
            PluginLang.msg(
                player,
                "system.lang-invalid",
                mapOf("lang" to langCode, "available" to available)
            )
            return
        }

        i18nManager.storage.write(player.uniqueId, langCode)
        i18nManager.service.invalidate(player.uniqueId)

        PluginLang.msg(
            player,
            "system.lang-changed",
            mapOf("lang" to langCode)
        )
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        if (args.size != 2) return emptyList()

        val plugin = (sender.server.pluginManager.getPlugin("InsureInv") as? dev.hytical.InsureInv)
            ?: return emptyList()

        return plugin.i18nManager.registry.languages
            .filter { it.lowercase().startsWith(args[1].lowercase()) }
            .sorted()
    }
}
