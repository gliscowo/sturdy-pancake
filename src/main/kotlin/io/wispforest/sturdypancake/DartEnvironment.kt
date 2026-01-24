package io.wispforest.sturdypancake

import io.wispforest.sturdypancake.bindings.DartBindings.*
import java.lang.foreign.MemorySegment

data class DartEnvironment(
    val mainIsolate: MemorySegment,
    val bindingsLibrary: MemorySegment,
    val bindingsResolver: MemorySegment,
    var outstandingMainIsolateMessages: Int = 0
) {
    fun runInMainIsolate(block: DartEnvironment.() -> Unit) {
        try {
            Dart_EnterIsolate(mainIsolate)
            Dart_EnterScope()

            block(this)
        } finally {
            Dart_ExitScope()
            Dart_ExitIsolate()
        }
    }

    val mainIsolateHasOutstandingMessages: Boolean
        get() = outstandingMainIsolateMessages > 0

    fun processOutstandingMainIsolateMessages() {
        while (outstandingMainIsolateMessages > 0) {
            Dart_HandleMessage()
            outstandingMainIsolateMessages--;
        }
    }
}
