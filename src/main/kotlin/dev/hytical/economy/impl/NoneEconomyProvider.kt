package dev.hytical.economy.impl

import dev.hytical.economy.EconomyProvider
import org.bukkit.OfflinePlayer

class NoneEconomyProvider : EconomyProvider {

    override fun isAvailable(): Boolean = false

    override fun getBalance(player: OfflinePlayer): Double = Double.MAX_VALUE

    override fun hasBalance(player: OfflinePlayer, amount: Double): Boolean = false

    override fun withdraw(player: OfflinePlayer, amount: Double): Boolean = false

    override fun deposit(player: OfflinePlayer, amount: Double): Boolean = false

    override fun formatAmount(amount: Double): String = String.format("%.2f", amount)
}