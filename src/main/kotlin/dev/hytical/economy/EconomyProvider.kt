package dev.hytical.economy

import org.bukkit.OfflinePlayer

sealed interface EconomyProvider {
    fun isAvailable(): Boolean

    fun getBalance(player: OfflinePlayer): Double

    fun hasBalance(player: OfflinePlayer, amount: Double): Boolean

    fun withdraw(player: OfflinePlayer, amount: Double): Boolean

    fun deposit(player: OfflinePlayer, amount: Double): Boolean

    fun formatAmount(amount: Double): String
}