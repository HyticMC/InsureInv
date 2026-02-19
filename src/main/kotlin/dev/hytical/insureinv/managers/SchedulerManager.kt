package dev.hytical.insureinv.managers

import com.tcoded.folialib.FoliaLib
import com.tcoded.folialib.wrapper.task.WrappedTask
import dev.hytical.insureinv.InsureInvPlugin
import org.bukkit.Location
import org.bukkit.entity.Entity
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

@Suppress("DEPRECATION")
class SchedulerManager(plugin: InsureInvPlugin) {
    private val foliaLib = FoliaLib(plugin)

    val isFolia: Boolean
        get() = foliaLib.isFolia

    fun runAsync(task: Runnable): CompletableFuture<Void> {
        val consumer = Consumer<WrappedTask> { task.run() }
        return foliaLib.scheduler.runAsync(consumer)
    }

    fun runSync(task: Runnable): CompletableFuture<Void> {
        val consumer = Consumer<WrappedTask> { task.run() }
        return foliaLib.scheduler.runNextTick(consumer)
    }

    fun runAtEntity(entity: Entity, task: Runnable): CompletableFuture<*> {
        val consumer = Consumer<WrappedTask> { task.run() }
        return foliaLib.scheduler.runAtEntity(entity, consumer)
    }

    fun runAtLocation(location: Location, task: Runnable): CompletableFuture<Void> {
        val consumer = Consumer<WrappedTask> { task.run() }
        return foliaLib.scheduler.runAtLocation(location, consumer)
    }

    fun runLater(delayTicks: Long, task: Runnable): CompletableFuture<Void> {
        val consumer = Consumer<WrappedTask> { task.run() }
        return foliaLib.scheduler.runLater(consumer, delayTicks)
    }

    fun runAsyncLater(delayTicks: Long, task: Runnable): CompletableFuture<Void> {
        val consumer = Consumer<WrappedTask> { task.run() }
        return foliaLib.scheduler.runLaterAsync(consumer, delayTicks)
    }

    fun runTimer(delayTicks: Long, periodTicks: Long, task: Runnable) {
        val consumer = Consumer<WrappedTask> { task.run() }
        foliaLib.scheduler.runTimer(consumer, delayTicks, periodTicks)
    }

    fun runAsyncTimer(delayTicks: Long, periodTicks: Long, task: Runnable) {
        val consumer = Consumer<WrappedTask> { task.run() }
        foliaLib.scheduler.runTimerAsync(consumer, delayTicks, periodTicks)
    }
}
