package dev.hytical.economy

import dev.hytical.HyticInv
import dev.hytical.economy.impl.NoneEconomyProvider

class EconomyRegistry(
    private val plugin: HyticInv
) {
    private val logger = plugin.logger
    private val config = plugin.configManager

    fun resolve(): EconomyProvider {
        val preferred = config.getEconomyProviderType()

        preferred.creator.invoke()?.let {
            logger.info("Using economy provider: $preferred")
            return it
        }

        logger.warning("Preferred economy $preferred not available, trying fallback...")

        val fallbackType = when (preferred) {
            EconomyProviderType.VAULT -> EconomyProviderType.PLAYER_POINTS
            EconomyProviderType.PLAYER_POINTS -> EconomyProviderType.VAULT
        }


        fallbackType.creator.invoke()?.let {
            logger.warning("Fallback to economy provider: $fallbackType")
            return it
        }

        logger.severe("No economy provider available. Using NONE provider.")
        return NoneEconomyProvider()
    }
}