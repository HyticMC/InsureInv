package dev.arclyx.storages.impl

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import dev.arclyx.HyticInv
import dev.arclyx.managers.ConfigManager
import dev.arclyx.model.PlayerData
import dev.arclyx.storages.StorageBackend
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class JsonStorage(
    private val plugin: HyticInv,
    private val configManager: ConfigManager
) : StorageBackend {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val dataMap = ConcurrentHashMap<UUID, PlayerData>()
    private val jsonFile: File by lazy {
        File(configManager.getJsonPath())
    }

    override fun initialize(): Boolean {
        return try {
            jsonFile.parentFile?.mkdirs()

            if (jsonFile.exists()) {
                loadFromFile()
            } else {
                jsonFile.createNewFile()
                saveToFile()
            }

            plugin.logger.info("JSON storage initialized successfully at ${jsonFile.absolutePath}")
            true
        } catch (e: Exception) {
            plugin.logger.severe("Failed to initialize JSON storage: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    private fun loadFromFile() {
        try {
            if (jsonFile.length() == 0L) {
                return
            }

            FileReader(jsonFile).use { reader ->
                val type = object : TypeToken<Map<String, PlayerData>>() {}.type
                val data: Map<String, PlayerData> = gson.fromJson(reader, type) ?: emptyMap()

                dataMap.clear()
                data.forEach { (uuidStr, playerData) ->
                    dataMap[UUID.fromString(uuidStr)] = playerData
                }
            }
        } catch (e: Exception) {
            plugin.logger.severe("Failed to load JSON data: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun saveToFile() {
        try {
            val dataToSave = dataMap.map { (uuid, data) ->
                uuid.toString() to data
            }.toMap()

            FileWriter(jsonFile).use { writer ->
                gson.toJson(dataToSave, writer)
            }
        } catch (e: Exception) {
            plugin.logger.severe("Failed to save JSON data: ${e.message}")
        }
    }

    override fun loadPlayerData(uuid: UUID): PlayerData? {
        return dataMap[uuid]
    }

    override fun savePlayerData(playerData: PlayerData) {
        dataMap[playerData.uuid] = playerData
        saveToFile()
    }

    override fun savePlayerDataAsync(playerData: PlayerData) {
        dataMap[playerData.uuid] = playerData
        CompletableFuture.runAsync {
            saveToFile()
        }
    }

    override fun close() {
        saveToFile()
        plugin.logger.info("JSON storage closed")
    }

    override fun isHealthy(): Boolean {
        return jsonFile.exists() && jsonFile.canRead() && jsonFile.canWrite()
    }

    override fun getName(): String = "JSON"

    fun getAllData(): Map<UUID, PlayerData> = dataMap.toMap()
}