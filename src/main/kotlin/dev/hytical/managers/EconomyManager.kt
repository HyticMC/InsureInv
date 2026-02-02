package dev.arclyx.managers

import dev.arclyx.HyticInv
import net.milkbowl.vault.economy.Economy
import org.bukkit.OfflinePlayer
import org.bukkit.plugin.RegisteredServiceProvider

class EconomyManager(
    private val plugin: HyticInv
) {
    private var economy: Economy? = null

    fun initialize(): Boolean {
        if (plugin.server.pluginManager.getPlugin("Vault") == null) {
            plugin.logger.warning("Vault not found! Economy features will be disabled.")
            return false
        }
        plugin.logger.info { "Using Vault as default Economy API" }
        val rsp: RegisteredServiceProvider<Economy>? =
            plugin.server.servicesManager.getRegistration(Economy::class.java)
        if (rsp == null) {
            plugin.logger.severe("No economy provider found! Please install an economy plugin.")
            return false
        }
        economy = rsp.provider
        plugin.logger.info("Hooked into economy provider: ${economy?.name}")
        return true
    }

    fun isAvailable(): Boolean {
        return economy != null
    }

    fun getBalance(player: OfflinePlayer): Double {
        return economy?.getBalance(player) ?: 0.0
    }

    fun hasBalance(player: OfflinePlayer, amount: Double): Boolean {
        return economy?.has(player, amount) ?: false
    }

    fun withdraw(player: OfflinePlayer, amount: Double): Boolean {
        val response = economy?.withdrawPlayer(player, amount)
        return response?.transactionSuccess() ?: false
    }

    fun deposit(player: OfflinePlayer, amount: Double): Boolean {
        val response = economy?.depositPlayer(player, amount)
        return response?.transactionSuccess() ?: false
    }

    fun formatAmount(amount: Double): String {
        return economy?.format(amount) ?: String.format("%.2f", amount)
    }
}