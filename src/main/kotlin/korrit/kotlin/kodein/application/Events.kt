package korrit.kotlin.kodein.application

import org.kodein.di.Kodein
import org.kodein.di.direct
import org.kodein.di.generic.bind
import org.kodein.di.generic.inSet
import org.kodein.di.generic.instance
import org.kodein.di.generic.setBinding

/**
 * Extension functions that abstract Kodein as a simple event dispatcher.
 *
 * First, you register your event types. They are internally used as binding tags.
 * Second, you register event callbacks.
 * Third, once your Kodein instance is created you can dispatch your events triggering registered callbacks.
 *
 * Please, note that you cannot pass any additional arguments to your callbacks with this simplified implementation.
 */

typealias KodeinEventCallback = Kodein.() -> Unit
typealias KodeinEventCallbacks = Set<KodeinEventCallback>

/**
 * Registers event types in Kodein and creates set binding for callbacks.
 *
 * There is no specific requirements for events. You are only required to use
 * the same OBJECT INSTANCE when registering callbacks and dispatching.
 */
fun Kodein.Builder.registerEvents(vararg events: Any) {
    events.forEach {
        bind(tag = it) from setBinding<KodeinEventCallback>()
    }
}

/**
 * Registers new callback for given event.
 */
fun Kodein.Builder.on(event: Any, callback: KodeinEventCallback) {
    bind<KodeinEventCallback>(tag = event).inSet() with instance(callback)
}

/**
 * Dispatches your event and triggers all registered callbacks.
 *
 * You can dispatch multiple times.
 * Callbacks are executed sequentially.
 */
fun Kodein.dispatchEvent(event: Any) {
    direct.instance<KodeinEventCallbacks>(tag = event).forEach {
        it()
    }
}
