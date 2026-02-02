package dev.arclyx.utils

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.entity.Player

object PlaceholderUtil {

    fun createResolver(vararg pairs: Pair<String, String>): TagResolver {
        val resolvers = pairs.map { (key, value) ->
            Placeholder.parsed(key, value)
        }
        return TagResolver.resolver(*resolvers.toTypedArray())
    }

    fun playerResolver(player: Player): TagResolver {
        return createResolver("player" to player.name)
    }

    fun chargesResolver(charges: Int, max: Int): TagResolver {
        return createResolver(
            "charges" to charges.toString(),
            "max" to max.toString()
        )
    }

    fun economyResolver(price: Double, balance: Double, amount: Int): TagResolver {
        return createResolver(
            "price" to String.format("%.2f", price),
            "balance" to String.format("%.2f", balance),
            "amount" to amount.toString()
        )
    }

    fun statusResolver(enabled: Boolean): TagResolver {
        return createResolver(
            "status" to if (enabled) "enabled" else "disabled",
            "toggle" to if (enabled) "ON" else "OFF"
        )
    }

    fun statsResolver(totalCharges: Int, usageCount: Int): TagResolver {
        return createResolver(
            "total_charges" to totalCharges.toString(),
            "usage_count" to usageCount.toString()
        )
    }

    fun methodResolver(method: String): TagResolver {
        return createResolver("method" to method)
    }

    fun paginationResolver(page: Int, total: Int): TagResolver {
        return createResolver(
            "page" to page.toString(),
            "total" to total.toString()
        )
    }
}