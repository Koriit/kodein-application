package com.korrit.kotlin.kodein.application

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.kodein.di.DI

internal class EventsTest {

    @Test
    fun `verify dispatcher works`() {
        val event = "type"
        var counter = 0

        val kodein = DI {
            registerEvents(event)

            on(event) {
                counter++
            }
        }

        kodein.dispatchEvent(event)

        assertEquals(1, counter)

        kodein.dispatchEvent(event)

        assertEquals(2, counter)
    }
}
