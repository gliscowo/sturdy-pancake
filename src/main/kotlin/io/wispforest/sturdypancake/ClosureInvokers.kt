package io.wispforest.sturdypancake

import io.wispforest.sturdypancake.bindings.DartBindings.*
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

object ClosureInvokers {
    @JvmStatic
    fun invokeUnary(closure: MemorySegment, argument: Any?): Any? = Arena.ofConfined().use { arena ->
        Dart_EnterIsolate(SturdyPancake.dartEnvironment.mainIsolate)
        Dart_EnterScope()
        try {
            val closureArgs = arena.allocate(C_POINTER, 1)
            closureArgs.setAtIndex(C_POINTER, 0, javaObjectToDartHandle(argument))

            val closureResult = Dart_InvokeClosure(closure, 1, closureArgs)
            SturdyPancake.dartEnvironment.processOutstandingMainIsolateMessages()

            return dartHandleToJavaObject(closureResult)
        } finally {
            Dart_ExitScope()
            Dart_ExitIsolate()
        }
    }
}