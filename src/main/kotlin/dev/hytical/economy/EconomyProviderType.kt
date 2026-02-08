package dev.hytical.economy

import dev.hytical.economy.impl.PlayerPointsEconomy
import dev.hytical.economy.impl.VaultEconomy

enum class EconomyProviderType(
    val creator: () -> EconomyProvider?
) {
    VAULT({ VaultEconomy.create() }),
    PLAYER_POINTS({ PlayerPointsEconomy.create() });

    companion object {
        fun fromString(value: String?): EconomyProviderType {
            return try {
                valueOf(value?.trim()?.uppercase() ?: "")
            } catch (_: IllegalArgumentException) {
                VAULT
            }
        }
    }
}