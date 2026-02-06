package dev.hytical.storages.impl

import dev.hytical.HyticInv
import dev.hytical.managers.ConfigManager
import dev.hytical.model.PlayerData
import dev.hytical.storages.StorageBackend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.util.*

class SQLiteStorage(
    private val plugin: HyticInv,
    private val configManager: ConfigManager
) : StorageBackend {

    private var connection: Connection? = null
    private val mutex = Mutex()
    private val dbFile: File by lazy {
        File(configManager.getSQLitePath())
    }

    override fun initialize(): Boolean {
        return try {
            dbFile.parentFile?.mkdirs()

            Class.forName("org.sqlite.JDBC")
            connection = DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}")

            createTables()
            plugin.logger.info("SQLite storage initialized successfully at ${dbFile.absolutePath}")
            true
        } catch (e: Exception) {
            plugin.logger.severe("Failed to initialize SQLite storage: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    private fun createTables() {
        connection?.createStatement()?.use { stmt ->
            stmt.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS hyticinv_players (
                    uuid TEXT PRIMARY KEY,
                    username TEXT NOT NULL,
                    charges INTEGER NOT NULL DEFAULT 0,
                    protection_enabled INTEGER NOT NULL DEFAULT 1,
                    total_charges_purchased INTEGER NOT NULL DEFAULT 0,
                    protection_activations INTEGER NOT NULL DEFAULT 0,
                    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """.trimIndent()
            )

            stmt.executeUpdate(
                """
                CREATE INDEX IF NOT EXISTS idx_username ON hyticinv_players(username)
            """.trimIndent()
            )
        }
    }

    override suspend fun loadPlayerData(uuid: UUID): PlayerData? = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                connection?.prepareStatement(
                    """
                SELECT uuid, username, charges, protection_enabled, 
                       total_charges_purchased, protection_activations
                FROM hyticinv_players WHERE uuid = ?
            """.trimIndent()
                )?.use { stmt ->
                    stmt.setString(1, uuid.toString())
                    stmt.executeQuery().use { rs ->
                        if (rs.next()) {
                            rs.toPlayerData()
                        } else {
                            null
                        }
                    }
                }
            } catch (e: Exception) {
                plugin.logger.severe("Failed to load player data for $uuid: ${e.message}")
                null
            }
        }
    }

    override suspend fun savePlayerData(playerData: PlayerData): Unit = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                connection?.prepareStatement(
                    """
                INSERT INTO hyticinv_players (uuid, username, charges, protection_enabled, 
                                           total_charges_purchased, protection_activations)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT(uuid) DO UPDATE SET
                    username = excluded.username,
                    charges = excluded.charges,
                    protection_enabled = excluded.protection_enabled,
                    total_charges_purchased = excluded.total_charges_purchased,
                    protection_activations = excluded.protection_activations,
                    last_updated = CURRENT_TIMESTAMP
            """.trimIndent()
                )?.use { stmt ->
                    stmt.setString(1, playerData.uuid.toString())
                    stmt.setString(2, playerData.username)
                    stmt.setInt(3, playerData.charges)
                    stmt.setInt(4, if (playerData.protectionEnabled) 1 else 0)
                    stmt.setInt(5, playerData.totalChargesPurchased)
                    stmt.setInt(6, playerData.protectionActivations)
                    stmt.executeUpdate()
                }
            } catch (e: Exception) {
                plugin.logger.severe("Failed to save player data for ${playerData.uuid}: ${e.message}")
            }
        }
    }

    override fun close() {
        connection?.close()
        plugin.logger.info("SQLite storage closed")
    }

    override fun isHealthy(): Boolean {
        return try {
            connection?.isValid(5) ?: false
        } catch (e: Exception) {
            false
        }
    }

    override fun getName(): String = "SQLite"

    private fun ResultSet.toPlayerData(): PlayerData {
        return PlayerData(
            uuid = UUID.fromString(getString("uuid")),
            username = getString("username"),
            charges = getInt("charges"),
            protectionEnabled = getInt("protection_enabled") == 1,
            totalChargesPurchased = getInt("total_charges_purchased"),
            protectionActivations = getInt("protection_activations")
        )
    }
}