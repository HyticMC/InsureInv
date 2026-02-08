package dev.hytical.economy

enum class EconomyProviderType {
    VAULT,
    PLAYER_POINTS;

    companion object {
        fun fromString(value: String?): EconomyProviderType {
            return try {
                valueOf(value?.trim()?.uppercase() ?: "")
            } catch (_: IllegalArgumentException) {
                VAULT
            }
        }
    }
}