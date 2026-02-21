package dev.hytical.insureinv.economy

import dev.hytical.insureinv.InsureInvPlugin
import dev.hytical.insureinv.economy.providers.NoneProvider
import dev.hytical.insureinv.economy.providers.PlayerPointsProvider
import dev.hytical.insureinv.economy.providers.VaultProvider
import java.util.logging.Logger

class EconomyRegistry(
    plugin: InsureInvPlugin
) {
    private val logger: Logger = plugin.logger
    private val config = plugin.configManager

    fun resolve(): EconomyProvider {
        val preferred = config.getEconomyProviderType()

        if (preferred == EconomyType.NONE) {
            logger.info("Economy provider set to NONE - economy features disabled")
            return NoneProvider
        }

        createProvider(preferred)?.let {
            logger.info("Using economy provider: $preferred")
            return it
        }

        logger.warning("Preferred economy $preferred not available, trying fallback...")

        for (type in EconomyType.entries) {
            if (type == preferred || type == EconomyType.NONE) continue
            createProvider(type)?.let {
                logger.warning("Fallback to economy provider: $type")
                return it
            }
        }

        logger.severe("No economy provider available. Using NONE provider.")
        return NoneProvider
    }

    private fun createProvider(type: EconomyType): EconomyProvider? {
        return when (type) {
            EconomyType.VAULT -> VaultProvider.create()
            EconomyType.PLAYER_POINTS -> PlayerPointsProvider.create()
            EconomyType.NONE -> NoneProvider
        }
    }
}