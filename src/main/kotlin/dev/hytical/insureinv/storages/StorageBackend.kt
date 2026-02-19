package dev.hytical.insureinv.storages

import dev.hytical.insureinv.models.PlayerDataModel
import java.util.*

interface StorageBackend {

    fun initialize(): Boolean

    fun close()

    suspend fun loadPlayerData(uuid: UUID): PlayerDataModel?

    suspend fun savePlayerData(playerDataModel: PlayerDataModel)

    fun isHealthy(): Boolean

    fun getName(): String
}