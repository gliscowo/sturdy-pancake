package io.wispforest.sturdypancake

import io.wispforest.sturdypancake.bindings.DartBindings.C_POINTER
import io.wispforest.sturdypancake.bindings.DartBindings.Dart_InvokeClosure
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

object ClosureInvokers {
    @JvmStatic
    fun invoke0(closure: MemorySegment): Any? = Arena.ofConfined().use { arena ->
        SturdyPancake.dartEnvironment.runInMainIsolate {
            val closureResult = Dart_InvokeClosure(closure, 0, MemorySegment.NULL)
            SturdyPancake.dartEnvironment.processOutstandingMainIsolateMessages()

            return@runInMainIsolate dartHandleToJavaObject(closureResult)
        }
    }

    @JvmStatic
    fun invoke1(closure: MemorySegment, arg0: Any?): Any? = Arena.ofConfined().use { arena ->
        SturdyPancake.dartEnvironment.runInMainIsolate {
            val closureArgs = arena.allocate(C_POINTER, 1)
            closureArgs.setAtIndex(C_POINTER, 0, javaObjectToDartHandle(arg0))

            val closureResult = Dart_InvokeClosure(closure, 1, closureArgs)
            SturdyPancake.dartEnvironment.processOutstandingMainIsolateMessages()

            return@runInMainIsolate dartHandleToJavaObject(closureResult)
        }
    }

    @JvmStatic
    fun invoke2(closure: MemorySegment, arg0: Any?, arg1: Any?): Any? = Arena.ofConfined().use { arena ->
        SturdyPancake.dartEnvironment.runInMainIsolate {
            val closureArgs = arena.allocate(C_POINTER, 2)
            closureArgs.setAtIndex(C_POINTER, 0, javaObjectToDartHandle(arg0))
            closureArgs.setAtIndex(C_POINTER, 1, javaObjectToDartHandle(arg1))

            val closureResult = Dart_InvokeClosure(closure, 2, closureArgs)
            SturdyPancake.dartEnvironment.processOutstandingMainIsolateMessages()

            return@runInMainIsolate dartHandleToJavaObject(closureResult)
        }
    }

    @JvmStatic
    fun invoke3(closure: MemorySegment, arg0: Any?, arg1: Any?, arg2: Any?): Any? = Arena.ofConfined().use { arena ->
        SturdyPancake.dartEnvironment.runInMainIsolate {
            val closureArgs = arena.allocate(C_POINTER, 3)
            closureArgs.setAtIndex(C_POINTER, 0, javaObjectToDartHandle(arg0))
            closureArgs.setAtIndex(C_POINTER, 1, javaObjectToDartHandle(arg1))
            closureArgs.setAtIndex(C_POINTER, 2, javaObjectToDartHandle(arg2))

            val closureResult = Dart_InvokeClosure(closure, 3, closureArgs)
            SturdyPancake.dartEnvironment.processOutstandingMainIsolateMessages()

            return@runInMainIsolate dartHandleToJavaObject(closureResult)
        }
    }

    @JvmStatic
    fun invoke4(closure: MemorySegment, arg0: Any?, arg1: Any?, arg2: Any?, arg3: Any?): Any? = Arena.ofConfined().use { arena ->
        SturdyPancake.dartEnvironment.runInMainIsolate {
            val closureArgs = arena.allocate(C_POINTER, 4)
            closureArgs.setAtIndex(C_POINTER, 0, javaObjectToDartHandle(arg0))
            closureArgs.setAtIndex(C_POINTER, 1, javaObjectToDartHandle(arg1))
            closureArgs.setAtIndex(C_POINTER, 2, javaObjectToDartHandle(arg2))
            closureArgs.setAtIndex(C_POINTER, 3, javaObjectToDartHandle(arg3))

            val closureResult = Dart_InvokeClosure(closure, 4, closureArgs)
            SturdyPancake.dartEnvironment.processOutstandingMainIsolateMessages()

            return@runInMainIsolate dartHandleToJavaObject(closureResult)
        }
    }

    @JvmStatic
    fun invoke5(closure: MemorySegment, arg0: Any?, arg1: Any?, arg2: Any?, arg3: Any?, arg4: Any?): Any? = Arena.ofConfined().use { arena ->
        SturdyPancake.dartEnvironment.runInMainIsolate {
            val closureArgs = arena.allocate(C_POINTER, 5)
            closureArgs.setAtIndex(C_POINTER, 0, javaObjectToDartHandle(arg0))
            closureArgs.setAtIndex(C_POINTER, 1, javaObjectToDartHandle(arg1))
            closureArgs.setAtIndex(C_POINTER, 2, javaObjectToDartHandle(arg2))
            closureArgs.setAtIndex(C_POINTER, 3, javaObjectToDartHandle(arg3))
            closureArgs.setAtIndex(C_POINTER, 4, javaObjectToDartHandle(arg4))

            val closureResult = Dart_InvokeClosure(closure, 5, closureArgs)
            SturdyPancake.dartEnvironment.processOutstandingMainIsolateMessages()

            return@runInMainIsolate dartHandleToJavaObject(closureResult)
        }
    }

    @JvmStatic
    fun invoke6(closure: MemorySegment, arg0: Any?, arg1: Any?, arg2: Any?, arg3: Any?, arg4: Any?, arg5: Any?): Any? = Arena.ofConfined().use { arena ->
        SturdyPancake.dartEnvironment.runInMainIsolate {
            val closureArgs = arena.allocate(C_POINTER, 6)
            closureArgs.setAtIndex(C_POINTER, 0, javaObjectToDartHandle(arg0))
            closureArgs.setAtIndex(C_POINTER, 1, javaObjectToDartHandle(arg1))
            closureArgs.setAtIndex(C_POINTER, 2, javaObjectToDartHandle(arg2))
            closureArgs.setAtIndex(C_POINTER, 3, javaObjectToDartHandle(arg3))
            closureArgs.setAtIndex(C_POINTER, 4, javaObjectToDartHandle(arg4))
            closureArgs.setAtIndex(C_POINTER, 5, javaObjectToDartHandle(arg5))

            val closureResult = Dart_InvokeClosure(closure, 6, closureArgs)
            SturdyPancake.dartEnvironment.processOutstandingMainIsolateMessages()

            return@runInMainIsolate dartHandleToJavaObject(closureResult)
        }
    }

    @JvmStatic
    fun invoke7(closure: MemorySegment, arg0: Any?, arg1: Any?, arg2: Any?, arg3: Any?, arg4: Any?, arg5: Any?, arg6: Any?): Any? = Arena.ofConfined().use { arena ->
        SturdyPancake.dartEnvironment.runInMainIsolate {
            val closureArgs = arena.allocate(C_POINTER, 7)
            closureArgs.setAtIndex(C_POINTER, 0, javaObjectToDartHandle(arg0))
            closureArgs.setAtIndex(C_POINTER, 1, javaObjectToDartHandle(arg1))
            closureArgs.setAtIndex(C_POINTER, 2, javaObjectToDartHandle(arg2))
            closureArgs.setAtIndex(C_POINTER, 3, javaObjectToDartHandle(arg3))
            closureArgs.setAtIndex(C_POINTER, 4, javaObjectToDartHandle(arg4))
            closureArgs.setAtIndex(C_POINTER, 5, javaObjectToDartHandle(arg5))
            closureArgs.setAtIndex(C_POINTER, 6, javaObjectToDartHandle(arg6))

            val closureResult = Dart_InvokeClosure(closure, 7, closureArgs)
            SturdyPancake.dartEnvironment.processOutstandingMainIsolateMessages()

            return@runInMainIsolate dartHandleToJavaObject(closureResult)
        }
    }

    @JvmStatic
    fun invoke8(closure: MemorySegment, arg0: Any?, arg1: Any?, arg2: Any?, arg3: Any?, arg4: Any?, arg5: Any?, arg6: Any?, arg7: Any?): Any? = Arena.ofConfined().use { arena ->
        SturdyPancake.dartEnvironment.runInMainIsolate {
            val closureArgs = arena.allocate(C_POINTER, 8)
            closureArgs.setAtIndex(C_POINTER, 0, javaObjectToDartHandle(arg0))
            closureArgs.setAtIndex(C_POINTER, 1, javaObjectToDartHandle(arg1))
            closureArgs.setAtIndex(C_POINTER, 2, javaObjectToDartHandle(arg2))
            closureArgs.setAtIndex(C_POINTER, 3, javaObjectToDartHandle(arg3))
            closureArgs.setAtIndex(C_POINTER, 4, javaObjectToDartHandle(arg4))
            closureArgs.setAtIndex(C_POINTER, 5, javaObjectToDartHandle(arg5))
            closureArgs.setAtIndex(C_POINTER, 6, javaObjectToDartHandle(arg6))
            closureArgs.setAtIndex(C_POINTER, 7, javaObjectToDartHandle(arg7))

            val closureResult = Dart_InvokeClosure(closure, 8, closureArgs)
            SturdyPancake.dartEnvironment.processOutstandingMainIsolateMessages()

            return@runInMainIsolate dartHandleToJavaObject(closureResult)
        }
    }

    @JvmStatic
    fun invoke9(closure: MemorySegment, arg0: Any?, arg1: Any?, arg2: Any?, arg3: Any?, arg4: Any?, arg5: Any?, arg6: Any?, arg7: Any?, arg8: Any?): Any? = Arena.ofConfined().use { arena ->
        SturdyPancake.dartEnvironment.runInMainIsolate {
            val closureArgs = arena.allocate(C_POINTER, 9)
            closureArgs.setAtIndex(C_POINTER, 0, javaObjectToDartHandle(arg0))
            closureArgs.setAtIndex(C_POINTER, 1, javaObjectToDartHandle(arg1))
            closureArgs.setAtIndex(C_POINTER, 2, javaObjectToDartHandle(arg2))
            closureArgs.setAtIndex(C_POINTER, 3, javaObjectToDartHandle(arg3))
            closureArgs.setAtIndex(C_POINTER, 4, javaObjectToDartHandle(arg4))
            closureArgs.setAtIndex(C_POINTER, 5, javaObjectToDartHandle(arg5))
            closureArgs.setAtIndex(C_POINTER, 6, javaObjectToDartHandle(arg6))
            closureArgs.setAtIndex(C_POINTER, 7, javaObjectToDartHandle(arg7))
            closureArgs.setAtIndex(C_POINTER, 8, javaObjectToDartHandle(arg8))

            val closureResult = Dart_InvokeClosure(closure, 9, closureArgs)
            SturdyPancake.dartEnvironment.processOutstandingMainIsolateMessages()

            return@runInMainIsolate dartHandleToJavaObject(closureResult)
        }
    }

    @JvmStatic
    fun invoke10(closure: MemorySegment, arg0: Any?, arg1: Any?, arg2: Any?, arg3: Any?, arg4: Any?, arg5: Any?, arg6: Any?, arg7: Any?, arg8: Any?, arg9: Any?): Any? = Arena.ofConfined().use { arena ->
        SturdyPancake.dartEnvironment.runInMainIsolate {
            val closureArgs = arena.allocate(C_POINTER, 10)
            closureArgs.setAtIndex(C_POINTER, 0, javaObjectToDartHandle(arg0))
            closureArgs.setAtIndex(C_POINTER, 1, javaObjectToDartHandle(arg1))
            closureArgs.setAtIndex(C_POINTER, 2, javaObjectToDartHandle(arg2))
            closureArgs.setAtIndex(C_POINTER, 3, javaObjectToDartHandle(arg3))
            closureArgs.setAtIndex(C_POINTER, 4, javaObjectToDartHandle(arg4))
            closureArgs.setAtIndex(C_POINTER, 5, javaObjectToDartHandle(arg5))
            closureArgs.setAtIndex(C_POINTER, 6, javaObjectToDartHandle(arg6))
            closureArgs.setAtIndex(C_POINTER, 7, javaObjectToDartHandle(arg7))
            closureArgs.setAtIndex(C_POINTER, 8, javaObjectToDartHandle(arg8))
            closureArgs.setAtIndex(C_POINTER, 9, javaObjectToDartHandle(arg9))

            val closureResult = Dart_InvokeClosure(closure, 10, closureArgs)
            SturdyPancake.dartEnvironment.processOutstandingMainIsolateMessages()

            return@runInMainIsolate dartHandleToJavaObject(closureResult)
        }
    }

    @JvmStatic
    fun invoke11(closure: MemorySegment, arg0: Any?, arg1: Any?, arg2: Any?, arg3: Any?, arg4: Any?, arg5: Any?, arg6: Any?, arg7: Any?, arg8: Any?, arg9: Any?, arg10: Any?): Any? = Arena.ofConfined().use { arena ->
        SturdyPancake.dartEnvironment.runInMainIsolate {
            val closureArgs = arena.allocate(C_POINTER, 11)
            closureArgs.setAtIndex(C_POINTER, 0, javaObjectToDartHandle(arg0))
            closureArgs.setAtIndex(C_POINTER, 1, javaObjectToDartHandle(arg1))
            closureArgs.setAtIndex(C_POINTER, 2, javaObjectToDartHandle(arg2))
            closureArgs.setAtIndex(C_POINTER, 3, javaObjectToDartHandle(arg3))
            closureArgs.setAtIndex(C_POINTER, 4, javaObjectToDartHandle(arg4))
            closureArgs.setAtIndex(C_POINTER, 5, javaObjectToDartHandle(arg5))
            closureArgs.setAtIndex(C_POINTER, 6, javaObjectToDartHandle(arg6))
            closureArgs.setAtIndex(C_POINTER, 7, javaObjectToDartHandle(arg7))
            closureArgs.setAtIndex(C_POINTER, 8, javaObjectToDartHandle(arg8))
            closureArgs.setAtIndex(C_POINTER, 9, javaObjectToDartHandle(arg9))
            closureArgs.setAtIndex(C_POINTER, 10, javaObjectToDartHandle(arg10))

            val closureResult = Dart_InvokeClosure(closure, 11, closureArgs)
            SturdyPancake.dartEnvironment.processOutstandingMainIsolateMessages()

            return@runInMainIsolate dartHandleToJavaObject(closureResult)
        }
    }

    @JvmStatic
    fun invoke12(closure: MemorySegment, arg0: Any?, arg1: Any?, arg2: Any?, arg3: Any?, arg4: Any?, arg5: Any?, arg6: Any?, arg7: Any?, arg8: Any?, arg9: Any?, arg10: Any?, arg11: Any?): Any? = Arena.ofConfined().use { arena ->
        SturdyPancake.dartEnvironment.runInMainIsolate {
            val closureArgs = arena.allocate(C_POINTER, 12)
            closureArgs.setAtIndex(C_POINTER, 0, javaObjectToDartHandle(arg0))
            closureArgs.setAtIndex(C_POINTER, 1, javaObjectToDartHandle(arg1))
            closureArgs.setAtIndex(C_POINTER, 2, javaObjectToDartHandle(arg2))
            closureArgs.setAtIndex(C_POINTER, 3, javaObjectToDartHandle(arg3))
            closureArgs.setAtIndex(C_POINTER, 4, javaObjectToDartHandle(arg4))
            closureArgs.setAtIndex(C_POINTER, 5, javaObjectToDartHandle(arg5))
            closureArgs.setAtIndex(C_POINTER, 6, javaObjectToDartHandle(arg6))
            closureArgs.setAtIndex(C_POINTER, 7, javaObjectToDartHandle(arg7))
            closureArgs.setAtIndex(C_POINTER, 8, javaObjectToDartHandle(arg8))
            closureArgs.setAtIndex(C_POINTER, 9, javaObjectToDartHandle(arg9))
            closureArgs.setAtIndex(C_POINTER, 10, javaObjectToDartHandle(arg10))
            closureArgs.setAtIndex(C_POINTER, 11, javaObjectToDartHandle(arg11))

            val closureResult = Dart_InvokeClosure(closure, 12, closureArgs)
            SturdyPancake.dartEnvironment.processOutstandingMainIsolateMessages()

            return@runInMainIsolate dartHandleToJavaObject(closureResult)
        }
    }

    @JvmStatic
    fun invoke13(closure: MemorySegment, arg0: Any?, arg1: Any?, arg2: Any?, arg3: Any?, arg4: Any?, arg5: Any?, arg6: Any?, arg7: Any?, arg8: Any?, arg9: Any?, arg10: Any?, arg11: Any?, arg12: Any?): Any? = Arena.ofConfined().use { arena ->
        SturdyPancake.dartEnvironment.runInMainIsolate {
            val closureArgs = arena.allocate(C_POINTER, 13)
            closureArgs.setAtIndex(C_POINTER, 0, javaObjectToDartHandle(arg0))
            closureArgs.setAtIndex(C_POINTER, 1, javaObjectToDartHandle(arg1))
            closureArgs.setAtIndex(C_POINTER, 2, javaObjectToDartHandle(arg2))
            closureArgs.setAtIndex(C_POINTER, 3, javaObjectToDartHandle(arg3))
            closureArgs.setAtIndex(C_POINTER, 4, javaObjectToDartHandle(arg4))
            closureArgs.setAtIndex(C_POINTER, 5, javaObjectToDartHandle(arg5))
            closureArgs.setAtIndex(C_POINTER, 6, javaObjectToDartHandle(arg6))
            closureArgs.setAtIndex(C_POINTER, 7, javaObjectToDartHandle(arg7))
            closureArgs.setAtIndex(C_POINTER, 8, javaObjectToDartHandle(arg8))
            closureArgs.setAtIndex(C_POINTER, 9, javaObjectToDartHandle(arg9))
            closureArgs.setAtIndex(C_POINTER, 10, javaObjectToDartHandle(arg10))
            closureArgs.setAtIndex(C_POINTER, 11, javaObjectToDartHandle(arg11))
            closureArgs.setAtIndex(C_POINTER, 12, javaObjectToDartHandle(arg12))

            val closureResult = Dart_InvokeClosure(closure, 13, closureArgs)
            SturdyPancake.dartEnvironment.processOutstandingMainIsolateMessages()

            return@runInMainIsolate dartHandleToJavaObject(closureResult)
        }
    }

    @JvmStatic
    fun invoke14(closure: MemorySegment, arg0: Any?, arg1: Any?, arg2: Any?, arg3: Any?, arg4: Any?, arg5: Any?, arg6: Any?, arg7: Any?, arg8: Any?, arg9: Any?, arg10: Any?, arg11: Any?, arg12: Any?, arg13: Any?): Any? = Arena.ofConfined().use { arena ->
        SturdyPancake.dartEnvironment.runInMainIsolate {
            val closureArgs = arena.allocate(C_POINTER, 14)
            closureArgs.setAtIndex(C_POINTER, 0, javaObjectToDartHandle(arg0))
            closureArgs.setAtIndex(C_POINTER, 1, javaObjectToDartHandle(arg1))
            closureArgs.setAtIndex(C_POINTER, 2, javaObjectToDartHandle(arg2))
            closureArgs.setAtIndex(C_POINTER, 3, javaObjectToDartHandle(arg3))
            closureArgs.setAtIndex(C_POINTER, 4, javaObjectToDartHandle(arg4))
            closureArgs.setAtIndex(C_POINTER, 5, javaObjectToDartHandle(arg5))
            closureArgs.setAtIndex(C_POINTER, 6, javaObjectToDartHandle(arg6))
            closureArgs.setAtIndex(C_POINTER, 7, javaObjectToDartHandle(arg7))
            closureArgs.setAtIndex(C_POINTER, 8, javaObjectToDartHandle(arg8))
            closureArgs.setAtIndex(C_POINTER, 9, javaObjectToDartHandle(arg9))
            closureArgs.setAtIndex(C_POINTER, 10, javaObjectToDartHandle(arg10))
            closureArgs.setAtIndex(C_POINTER, 11, javaObjectToDartHandle(arg11))
            closureArgs.setAtIndex(C_POINTER, 12, javaObjectToDartHandle(arg12))
            closureArgs.setAtIndex(C_POINTER, 13, javaObjectToDartHandle(arg13))

            val closureResult = Dart_InvokeClosure(closure, 14, closureArgs)
            SturdyPancake.dartEnvironment.processOutstandingMainIsolateMessages()

            return@runInMainIsolate dartHandleToJavaObject(closureResult)
        }
    }

    @JvmStatic
    fun invoke15(closure: MemorySegment, arg0: Any?, arg1: Any?, arg2: Any?, arg3: Any?, arg4: Any?, arg5: Any?, arg6: Any?, arg7: Any?, arg8: Any?, arg9: Any?, arg10: Any?, arg11: Any?, arg12: Any?, arg13: Any?, arg14: Any?): Any? = Arena.ofConfined().use { arena ->
        SturdyPancake.dartEnvironment.runInMainIsolate {
            val closureArgs = arena.allocate(C_POINTER, 15)
            closureArgs.setAtIndex(C_POINTER, 0, javaObjectToDartHandle(arg0))
            closureArgs.setAtIndex(C_POINTER, 1, javaObjectToDartHandle(arg1))
            closureArgs.setAtIndex(C_POINTER, 2, javaObjectToDartHandle(arg2))
            closureArgs.setAtIndex(C_POINTER, 3, javaObjectToDartHandle(arg3))
            closureArgs.setAtIndex(C_POINTER, 4, javaObjectToDartHandle(arg4))
            closureArgs.setAtIndex(C_POINTER, 5, javaObjectToDartHandle(arg5))
            closureArgs.setAtIndex(C_POINTER, 6, javaObjectToDartHandle(arg6))
            closureArgs.setAtIndex(C_POINTER, 7, javaObjectToDartHandle(arg7))
            closureArgs.setAtIndex(C_POINTER, 8, javaObjectToDartHandle(arg8))
            closureArgs.setAtIndex(C_POINTER, 9, javaObjectToDartHandle(arg9))
            closureArgs.setAtIndex(C_POINTER, 10, javaObjectToDartHandle(arg10))
            closureArgs.setAtIndex(C_POINTER, 11, javaObjectToDartHandle(arg11))
            closureArgs.setAtIndex(C_POINTER, 12, javaObjectToDartHandle(arg12))
            closureArgs.setAtIndex(C_POINTER, 13, javaObjectToDartHandle(arg13))
            closureArgs.setAtIndex(C_POINTER, 14, javaObjectToDartHandle(arg14))

            val closureResult = Dart_InvokeClosure(closure, 15, closureArgs)
            SturdyPancake.dartEnvironment.processOutstandingMainIsolateMessages()

            return@runInMainIsolate dartHandleToJavaObject(closureResult)
        }
    }

    @JvmStatic
    fun invoke16(closure: MemorySegment, arg0: Any?, arg1: Any?, arg2: Any?, arg3: Any?, arg4: Any?, arg5: Any?, arg6: Any?, arg7: Any?, arg8: Any?, arg9: Any?, arg10: Any?, arg11: Any?, arg12: Any?, arg13: Any?, arg14: Any?, arg15: Any?): Any? = Arena.ofConfined().use { arena ->
        SturdyPancake.dartEnvironment.runInMainIsolate {
            val closureArgs = arena.allocate(C_POINTER, 16)
            closureArgs.setAtIndex(C_POINTER, 0, javaObjectToDartHandle(arg0))
            closureArgs.setAtIndex(C_POINTER, 1, javaObjectToDartHandle(arg1))
            closureArgs.setAtIndex(C_POINTER, 2, javaObjectToDartHandle(arg2))
            closureArgs.setAtIndex(C_POINTER, 3, javaObjectToDartHandle(arg3))
            closureArgs.setAtIndex(C_POINTER, 4, javaObjectToDartHandle(arg4))
            closureArgs.setAtIndex(C_POINTER, 5, javaObjectToDartHandle(arg5))
            closureArgs.setAtIndex(C_POINTER, 6, javaObjectToDartHandle(arg6))
            closureArgs.setAtIndex(C_POINTER, 7, javaObjectToDartHandle(arg7))
            closureArgs.setAtIndex(C_POINTER, 8, javaObjectToDartHandle(arg8))
            closureArgs.setAtIndex(C_POINTER, 9, javaObjectToDartHandle(arg9))
            closureArgs.setAtIndex(C_POINTER, 10, javaObjectToDartHandle(arg10))
            closureArgs.setAtIndex(C_POINTER, 11, javaObjectToDartHandle(arg11))
            closureArgs.setAtIndex(C_POINTER, 12, javaObjectToDartHandle(arg12))
            closureArgs.setAtIndex(C_POINTER, 13, javaObjectToDartHandle(arg13))
            closureArgs.setAtIndex(C_POINTER, 14, javaObjectToDartHandle(arg14))
            closureArgs.setAtIndex(C_POINTER, 15, javaObjectToDartHandle(arg15))

            val closureResult = Dart_InvokeClosure(closure, 16, closureArgs)
            SturdyPancake.dartEnvironment.processOutstandingMainIsolateMessages()

            return@runInMainIsolate dartHandleToJavaObject(closureResult)
        }
    }
}

fun main() {
    println(
        """
        @JvmStatic
        fun invoke0(closure: MemorySegment): Any? = Arena.ofConfined().use { arena ->
            SturdyPancake.dartEnvironment.runInMainIsolate {
                val closureResult = Dart_InvokeClosure(closure, 0, MemorySegment.NULL)
                SturdyPancake.dartEnvironment.processOutstandingMainIsolateMessages()

                return@runInMainIsolate dartHandleToJavaObject(closureResult)
            }
        }

        """.trimIndent()
    )

    repeat(16) { argCount ->
        val argCount = argCount + 1

        val functionArguments = (0..<argCount).joinToString { "arg$it: Any?" }
        val closureArguments = (0..<argCount).joinToString(separator = "\n                    ") {
            "closureArgs.setAtIndex(C_POINTER, $it, javaObjectToDartHandle(arg$it))"
        }

        println(
            """
            @JvmStatic
            fun invoke$argCount(closure: MemorySegment, $functionArguments): Any? = Arena.ofConfined().use { arena ->
                SturdyPancake.dartEnvironment.runInMainIsolate {
                    val closureArgs = arena.allocate(C_POINTER, $argCount)
                    $closureArguments

                    val closureResult = Dart_InvokeClosure(closure, $argCount, closureArgs)
                    SturdyPancake.dartEnvironment.processOutstandingMainIsolateMessages()

                    return@runInMainIsolate dartHandleToJavaObject(closureResult)
                }
            }

            """.trimIndent()
        )
    }
}