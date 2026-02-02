package dev.arclyx.model

import java.util.UUID

data class PlayerData(
    val uuid: UUID,
    var username: String,
    var charges: Int = 0,
    var protectionEnabled: Boolean = true,
    var totalChargesPurchased: Int = 0,
    var protectionActivations: Int = 0
) {
    fun hasCharges(): Boolean = charges > 0

    fun consumeCharge(): Boolean {
        return if (hasCharges()) {
            charges--
            protectionActivations++
            true
        } else {
            false
        }
    }

    fun addCharges(amount: Int) {
        charges += amount
        totalChargesPurchased += amount
    }

    fun updateCharges(amount: Int) {
        charges = amount
    }
}
