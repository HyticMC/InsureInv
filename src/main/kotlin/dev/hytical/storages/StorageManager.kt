package dev.arclyx.storages

import dev.arclyx.HyticInv
import dev.arclyx.managers.ConfigManager
import dev.arclyx.model.PlayerData
import dev.arclyx.storages.impl.JsonStorage
import dev.arclyx.storages.impl.MySQLStorage
import dev.arclyx.storages.impl.SQLiteStorage
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class StorageManager(
    private val plugin: HyticInv,
    private val configManager: ConfigManager
) {
    private var currentBackend: StorageBackend? = null
    private val globalCache = ConcurrentHashMap<UUID, PlayerData>()

    fun initialize(): Boolean {
        val preferedMethod = configManager.getStorageMethod()
        plugin.logger.info("Attempting to initialize storage with method: $preferedMethod")

        val backends = when(preferedMethod) {
            StorageType.MYSQL -> listOf(
                { MySQLStorage(plugin, configManager) },
                { SQLiteStorage(plugin, configManager) },
                { JsonStorage(plugin, configManager) }
            )

            StorageType.SQLITE -> listOf(
                { SQLiteStorage(plugin, configManager) },
                { JsonStorage(plugin, configManager) }
            )

            StorageType.JSON -> listOf(
                { JsonStorage(plugin, configManager) }
            )

            else -> {
                plugin.logger.warning("Unknown storage method '${preferedMethod.name}', defaulting to SQLite")
                listOf(
                    { SQLiteStorage(plugin, configManager) },
                    { JsonStorage(plugin, configManager) }
                )
            }
        }

        for(backendFactory in backends) {
            val backend = backendFactory()
            if(backend.initialize()) {
                currentBackend = backend
                plugin.logger.info("Successfully initialized ${backend.getName()} storage")
                return true
            } else {
                plugin.logger.warning("Failed to initialize ${backend.getName()} storage, trying next option...")
            }
        }

        plugin.logger.severe("All storage backends failed to initialize!")
        return false
    }

    fun getPlayerData(player: Player): PlayerData {
        return getPlayerData(player.uniqueId, player.name)
    }

    fun getPlayerData(uuid: UUID, username: String): PlayerData {
        return globalCache.getOrPut(uuid) {
            currentBackend?.loadPlayerData(uuid) ?: PlayerData(uuid, username)
        }
    }

    fun savePlayerData(playerData: PlayerData, async: Boolean = true) {
        globalCache[playerData.uuid] = playerData
        if (async) {
            currentBackend?.savePlayerDataAsync(playerData)
        } else {
            currentBackend?.savePlayerData(playerData)
        }
    }

    fun saveAll(async: Boolean = false) {
        globalCache.values.forEach { playerData ->
            if (async) {
                currentBackend?.savePlayerDataAsync(playerData)
            } else {
                currentBackend?.savePlayerData(playerData)
            }
        }
    }

    fun shutdown() {
        plugin.logger.info("Saving all cached player data...")
        saveAll(async = false)
        currentBackend?.close()
        globalCache.clear()
    }

    fun isHealthy(): Boolean {
        return currentBackend?.isHealthy() ?: false
    }

    fun getCurrentBackendName(): String {
        return currentBackend?.getName() ?: "None"
    }

    fun reload(): Boolean {
        shutdown()
        globalCache.clear()
        return initialize()
    }
}