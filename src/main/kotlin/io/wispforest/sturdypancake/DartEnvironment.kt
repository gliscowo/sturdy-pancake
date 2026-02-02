package io.wispforest.sturdypancake

import io.wispforest.sturdypancake.bindings.DartBindings.*
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

data class DartEnvironment(
    val mainIsolate: MemorySegment,
    val bindingsLibrary: MemorySegment,
    val bindingsResolver: MemorySegment,
    var outstandingMainIsolateMessages: Int = 0
) {
    fun <T> runInMainIsolate(block: DartEnvironment.() -> T): T {
        if (Dart_CurrentIsolate() == mainIsolate) {
            try {
                Dart_EnterScope()
                return block(this)
            } finally {
                Dart_ExitScope()
            }
        } else {
            try {
                Dart_EnterIsolate(mainIsolate)
                Dart_EnterScope()

                return block(this)
            } finally {
                Dart_ExitScope()
                Dart_ExitIsolate()
            }
        }
    }

    fun invoke(
        symbol: String,
        vararg arguments: MemorySegment,
        target: MemorySegment = bindingsLibrary
    ): MemorySegment = Arena.ofConfined().use { arena ->
        val invokeArgs: MemorySegment
        if (arguments.isNotEmpty()) {
            invokeArgs = arena.allocate(C_POINTER, arguments.size.toLong())

            arguments.forEachIndexed { idx, arg ->
                invokeArgs.setAtIndex(C_POINTER, idx.toLong(), arg)
            }
        } else {
            invokeArgs = MemorySegment.NULL
        }

        return Dart_Invoke(
            target,
            Dart_NewStringFromCString(arena.allocateFrom(symbol)),
            arguments.size,
            invokeArgs
        )
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
