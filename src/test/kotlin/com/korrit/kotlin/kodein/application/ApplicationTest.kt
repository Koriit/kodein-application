package com.korrit.kotlin.kodein.application

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.kodein.di.direct
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

internal class ApplicationTest {

    /** Class fixture. */
    class SomeAutoCloseable : AutoCloseable {
        /** State fixture. */
        var closed = false

        override fun close() {
            closed = true
        }
    }

    @Test
    fun `should close AutoCloseable on stop`() {
        val app = kodeinApplication {
            bind() from singleton { SomeAutoCloseable() }
        }

        app.dispatchEvent(ApplicationEvents.Stop)

        assertEquals(true, app.direct.instance<SomeAutoCloseable>().closed)
    }
}
