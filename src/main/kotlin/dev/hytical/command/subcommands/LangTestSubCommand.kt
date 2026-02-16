package dev.hytical.command.subcommands

import dev.hytical.command.CommandContext
import dev.hytical.command.InsureInvSubCommand
import dev.hytical.i18n.PluginLang
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class LangTestSubCommand : InsureInvSubCommand {
    override val name = "langtest"
    override val permission = "insureinv.admin"
    override val requiresPlayer = true

    override fun execute(context: CommandContext) {
        val player = context.playerOrThrow
        val lang = context.plugin.i18nManager.service.getLanguage(player.uniqueId)
        PluginLang.msg(
            player,
            "system.lang-test",
            mapOf("lang" to lang)
        )
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> = emptyList()
}
