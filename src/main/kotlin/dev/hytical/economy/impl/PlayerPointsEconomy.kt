package dev.hytical.economy.impl

import dev.hytical.economy.EconomyProvider
import org.black_ixx.playerpoints.PlayerPoints
import org.black_ixx.playerpoints.PlayerPointsAPI
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.util.logging.Logger

class PlayerPointsEconomy(private val ppAPI: PlayerPointsAPI) : EconomyProvider {

    override fun isAvailable(): Boolean = true

    override fun getBalance(player: OfflinePlayer): Double {
        val uuid = player.uniqueId
        val points = ppAPI.look(uuid)
        return if (points >= 0) points.toDouble() else 0.0
    }

    override fun hasBalance(player: OfflinePlayer, amount: Double): Boolean {
        val uuid = player.uniqueId
        val points = ppAPI.look(uuid)
        return points >= 0 && points >= amount.toInt()
    }

    override fun withdraw(player: OfflinePlayer, amount: Double): Boolean {
        if (amount <= 0) return false
        val uuid = player.uniqueId
        val value = amount.toInt()
        if (value <= 0) return false

        val current = ppAPI.look(uuid)
        if (current < 0 || current < value) return false

        return ppAPI.take(uuid, value)
    }

    override fun deposit(player: OfflinePlayer, amount: Double): Boolean {
        if (amount <= 0) return false
        val uuid = player.uniqueId
        val value = amount.toInt()
        if (value <= 0) return false

        return ppAPI.give(uuid, value)
    }

    override fun formatAmount(amount: Double): String {
        val value = amount.toInt()
        return "%,d".format(value)
    }

    companion object {
        fun create(): EconomyProvider? {
            val plugin = Bukkit.getPluginManager().getPlugin("PlayerPoints")
                ?: return null

            if (!plugin.isEnabled) return null

            val api = (plugin as PlayerPoints).api
            return PlayerPointsEconomy(api)
        }
    }
}