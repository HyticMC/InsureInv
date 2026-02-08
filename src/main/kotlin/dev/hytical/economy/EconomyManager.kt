package dev.hytical.economy

import dev.hytical.HyticInv
import org.bukkit.OfflinePlayer

class EconomyManager(
    private val plugin: HyticInv
) {
    private lateinit var provider: EconomyProvider

    fun initialize() {
        provider = EconomyRegistry(plugin).resolve()
    }

    fun isAvailable(): Boolean {
        return provider.isAvailable()
    }

    fun getBalance(player: OfflinePlayer): Double {
        return provider.getBalance(player)
    }

    fun hasBalance(player: OfflinePlayer, amount: Double): Boolean {
        return provider.hasBalance(player, amount)
    }

    fun withdraw(player: OfflinePlayer, amount: Double): Boolean {
        return provider.withdraw(player, amount)
    }

    fun deposit(player: OfflinePlayer, amount: Double): Boolean {
        return provider.deposit(player, amount)
    }

    fun formatAmount(amount: Double): String = provider.formatAmount(amount)
}