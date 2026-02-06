package dev.hytical.storages

import dev.hytical.HyticInv
import dev.hytical.managers.ConfigManager
import dev.hytical.model.PlayerData
import dev.hytical.storages.impl.JsonStorage
import dev.hytical.storages.impl.MySQLStorage
import dev.hytical.storages.impl.SQLiteStorage
import kotlinx.coroutines.*
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds

class StorageManager(
    private val plugin: HyticInv,
    private val configManager: ConfigManager
) {
    private var currentBackend: StorageBackend? = null
    private val globalCache = ConcurrentHashMap<UUID, PlayerData>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun initialize(): Boolean {
        val preferredMethod = configManager.getStorageMethod()
        plugin.logger.info("Attempting to initialize storage with method: $preferredMethod")

        val backends = when (preferredMethod) {
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
        }

        for (backendFactory in backends) {
            val backend = backendFactory()
            if (backend.initialize()) {
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
            runBlocking {
                currentBackend?.loadPlayerData(uuid)
            } ?: PlayerData(uuid, username)
        }
    }

    fun savePlayerData(playerData: PlayerData, async: Boolean = true) {
        globalCache[playerData.uuid] = playerData
        if (async) {
            scope.launch {
                currentBackend?.savePlayerData(playerData)
            }
        } else {
            runBlocking {
                currentBackend?.savePlayerData(playerData)
            }
        }
    }

    fun saveAll(async: Boolean = false) {
        if (async) {
            globalCache.values.forEach { playerData ->
                scope.launch {
                    currentBackend?.savePlayerData(playerData)
                }
            }
        } else {
            runBlocking {
                globalCache.values.forEach { playerData ->
                    currentBackend?.savePlayerData(playerData)
                }
            }
        }
    }

    fun shutdown() {
        plugin.logger.info("Saving all cached player data...")

        runBlocking {
            withTimeoutOrNull(10.seconds) {
                globalCache.values.forEach { playerData ->
                    currentBackend?.savePlayerData(playerData)
                }
            } ?: plugin.logger.warning("Shutdown save timed out after 10 seconds")
        }

        scope.cancel()
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
        return initialize()
    }
}