package com.korrit.kotlin.kodein.application

import org.kodein.di.DI
import org.kodein.di.bindSet
import org.kodein.di.direct
import org.kodein.di.inSet
import org.kodein.di.instance

/**
 * Extension functions that abstract DI as a simple event dispatcher.
 *
 * First, you register your event types. They are internally used as binding tags.
 * Second, you register event callbacks.
 * Third, once your DI instance is created you can dispatch your events triggering registered callbacks.
 *
 * Please, note that you cannot pass any additional arguments to your callbacks with this simplified implementation.
 */

typealias KodeinEventCallback = DI.() -> Unit
typealias KodeinEventCallbacks = Set<KodeinEventCallback>

/**
 * Registers event types in DI and creates set binding for callbacks.
 *
 * There is no specific requirements for events. You are only required to use
 * the same OBJECT INSTANCE when registering callbacks and dispatching.
 */
fun DI.Builder.registerEvents(vararg events: Any) {
    events.forEach {
        bindSet<KodeinEventCallback>(tag = it)
    }
}

/**
 * Registers new callback for given event.
 */
fun DI.Builder.on(event: Any, callback: KodeinEventCallback) {
    inSet<KodeinEventCallback>(tag = event) { instance(callback) }
}

/**
 * Dispatches your event and triggers all registered callbacks.
 *
 * You can dispatch multiple times.
 * Callbacks are executed sequentially.
 */
fun DI.dispatchEvent(event: Any) {
    direct.instance<KodeinEventCallbacks>(tag = event).forEach {
        it()
    }
}
