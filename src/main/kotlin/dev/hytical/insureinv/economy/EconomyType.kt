package dev.hytical.insureinv.economy

enum class EconomyType {
    VAULT,
    PLAYER_POINTS,
    NONE;

    companion object {
        fun fromString(value: String?): EconomyType {
            return try {
                valueOf(value?.trim()?.uppercase() ?: "")
            } catch (_: IllegalArgumentException) {
                VAULT
            }
        }
    }
}