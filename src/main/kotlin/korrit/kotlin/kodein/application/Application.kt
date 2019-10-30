package korrit.kotlin.kodein.application

import korrit.kotlin.kodein.application.ApplicationEvents.Start
import korrit.kotlin.kodein.application.ApplicationEvents.Stop
import org.kodein.di.Kodein
import org.kodein.di.direct
import org.kodein.di.generic.allInstances
import org.slf4j.LoggerFactory
import java.lang.management.ManagementFactory
import java.util.concurrent.CyclicBarrier

/**
 * Extension functions that ease making applications with Kodein container as a core.
 */

enum class ApplicationEvents {
    /**
     * Startup when everything is configured.
     * Delegating server starting, workers creation, etc. helps later with testing.
     */
    Start,

    /**
     * Stopping application to close it.
     * Perfect time to gracefully close servers, workers and general cleanup.
     */
    Stop
}

/**
 * Helper function that wraps normal Kodein builder and registers some helpers.
 *
 * Registers a stop callback that closes AutoCloseable instances.
 */
fun kodeinApplication(allowSilentOverride: Boolean = false, init: Kodein.MainBuilder.() -> Unit): Kodein {
    return Kodein(allowSilentOverride) {
        registerEvents(*ApplicationEvents.values())

        on(Stop) {
            // FIXME: this creates lazy AutoCloseable instances which were not created yet
            // Cleanup AutoCloseable instances
            direct.allInstances<AutoCloseable>().forEach {
                it.close()
            }
        }

        init()
    }
}

/**
 * Main application function. Registers shutdown hooks, dispatches application events and blocks until shutdown.
 */
fun Kodein.run() {
    val LOG = LoggerFactory.getLogger("koriit.kotlin.kodein.application.ApplicationKt")
    try {
        LOG.info("Application configured, starting...")

        // Execute startup callbacks like starting server connectors, creating workers, etc.
        dispatchEvent(Start)

        val finish = CyclicBarrier(2)
        val main = Thread.currentThread()
        Runtime.getRuntime().addShutdownHook(Thread {
            try {
                // Trigger cleanup callbacks like stopping server, etc.
                dispatchEvent(Stop)

            } catch (e: Throwable) {
                LOG.error("Unexpected problem: ${e.message}", e)

            } finally {
                finish.await()
                // Sometimes JVM might not wait for `main` to finish when receiving interrupt
                main.join()
            }
        })

        LOG.info("Application startup finished: " + (ManagementFactory.getRuntimeMXBean().uptime / 1000.0) + "s")

        // Block until shutdown
        finish.await()
        LOG.info("Application stopped")

    } catch (e: Throwable) {
        LOG.error("Application stopped unexpectedly: ${e.message}", e)
    }
}