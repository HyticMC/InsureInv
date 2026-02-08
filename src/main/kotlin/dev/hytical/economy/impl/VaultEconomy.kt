package dev.hytical.economy.impl

import dev.hytical.HyticInv
import dev.hytical.economy.EconomyProvider
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.util.logging.Logger

class VaultEconomy(
    private val economy: Economy?
) : EconomyProvider {
    override fun isAvailable(): Boolean {
        return economy != null
    }

    override fun getBalance(player: OfflinePlayer): Double {
        return economy?.getBalance(player) ?: 0.0
    }

    override fun hasBalance(player: OfflinePlayer, amount: Double): Boolean {
        return economy?.has(player, amount) ?: false
    }

    override fun withdraw(player: OfflinePlayer, amount: Double): Boolean {
        val response = economy?.withdrawPlayer(player, amount)
        return response?.transactionSuccess() ?: false
    }

    override fun deposit(player: OfflinePlayer, amount: Double): Boolean {
        val response = economy?.depositPlayer(player, amount)
        return response?.transactionSuccess() ?: false
    }

    override fun formatAmount(amount: Double): String {
        return economy?.format(amount) ?: String.format("%.2f", amount)
    }

    companion object {
        fun create(): EconomyProvider? {
            val pm = Bukkit.getPluginManager()

            val vault = pm.getPlugin("Vault") ?: return null
            if (!vault.isEnabled) return null

            val rsp = Bukkit.getServicesManager()
                .getRegistration(Economy::class.java)
                ?: return null

            val provider = rsp.provider ?: return null

            return VaultEconomy(provider)
        }
    }
}