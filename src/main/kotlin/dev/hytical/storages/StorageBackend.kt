package dev.hytical.storages

import dev.hytical.model.PlayerData
import java.util.*

interface StorageBackend {

    fun initialize(): Boolean

    fun close()

    suspend fun loadPlayerData(uuid: UUID): PlayerData?

    suspend fun savePlayerData(playerData: PlayerData)

    fun isHealthy(): Boolean

    fun getName(): String
}