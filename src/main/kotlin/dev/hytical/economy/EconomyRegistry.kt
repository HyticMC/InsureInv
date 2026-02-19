package dev.hytical.economy

import dev.hytical.InsureInv
import dev.hytical.economy.impl.NoneEconomyProvider
import dev.hytical.economy.impl.PlayerPointsEconomy
import dev.hytical.economy.impl.VaultEconomy
import java.util.logging.Logger

class EconomyRegistry(
    private val plugin: InsureInv
) {
    private val logger: Logger = plugin.logger
    private val config = plugin.configManager

    fun resolve(): EconomyProvider {
        val preferred = config.getEconomyProviderType()

        createProvider(preferred)?.let {
            logger.info("Using economy provider: $preferred")
            return it
        }

        logger.warning("Preferred economy $preferred not available, trying fallback...")

        for (type in EconomyProviderType.entries) {
            if (type == preferred) continue
            createProvider(type)?.let {
                logger.warning("Fallback to economy provider: $type")
                return it
            }
        }

        logger.severe("No economy provider available. Using NONE provider.")
        return NoneEconomyProvider
    }

    private fun createProvider(type: EconomyProviderType): EconomyProvider? {
        return when (type) {
            EconomyProviderType.VAULT -> VaultEconomy.create()
            EconomyProviderType.PLAYER_POINTS -> PlayerPointsEconomy.create()
        }
    }
}