package dev.hytical.economy

import dev.hytical.HyticInv
import java.util.logging.Logger

class EconomyRegistry(
    private val plugin: HyticInv
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