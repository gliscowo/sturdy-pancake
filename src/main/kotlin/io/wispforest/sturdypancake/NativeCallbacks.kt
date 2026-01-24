package io.wispforest.sturdypancake

import io.wispforest.sturdypancake.bindings.DartBindings.*
import org.slf4j.LoggerFactory
import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemorySegment
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.invoke.VarHandle
import java.lang.reflect.Modifier

object NativeCallbacks {

    @JvmStatic
    fun setupIsolate(): Unit = Arena.ofConfined().use { arena ->
        if (!SturdyPancake.dartEnvironmentInitialized) {
            return
        }

        val builtinLibrary = Dart_LookupLibrary(Dart_NewStringFromCString(arena.allocateFrom("dart:_internal")))
        val printClosure = Dart_GetField(SturdyPancake.dartEnvironment.bindingsLibrary, Dart_NewStringFromCString(arena.allocateFrom("_pancakePrint")))

        Dart_SetField(
            builtinLibrary,
            Dart_NewStringFromCString(arena.allocateFrom("_printClosure")),
            printClosure
        )
    }

    private val allocatedBindings: MutableMap<String, MemorySegment> = mutableMapOf()

    @JvmStatic
    fun resolveJavaBinding(
        name: MemorySegment /* Dart_Handle */,
        numArgs: Int,
        autoSetupScope: MemorySegment /* bool* */
    ): MemorySegment /* Dart_NativeFunction */ {
        val funcName = dartStringToJavaString(name)
        if (!funcName.startsWith("::")) {
            return MemorySegment.NULL
        }

        if (funcName in allocatedBindings) {
            return allocatedBindings[funcName]!!
        }

        val targetParts = funcName.substring("::".length).split("::")
        if (targetParts.size != 2 && targetParts.size != 3) {
            return MemorySegment.NULL
        }

        val className = targetParts[0]
        val targetName = targetParts[1]

        val theClass: Class<*>
        try {
            theClass = Class.forName(className)
        } catch (_: ClassNotFoundException) {
            SturdyPancake.LOGGER.error("unable to bind class $className")
            return MemorySegment.NULL
        }

        val targetHandle: MethodHandle
        if (targetParts.size == 3) {
            val accessMode = targetParts[2]
            if (accessMode != "get" && accessMode != "set") {
                return MemorySegment.NULL
            }

            val theField = theClass.fields.first { it.name == targetName }
            if (theField == null) {
                SturdyPancake.LOGGER.error("unable to bind field $targetName on class $className")
                return MemorySegment.NULL
            }

            targetHandle = MethodHandles.lookup().unreflectVarHandle(theField).toMethodHandle(
                if (accessMode == "get") VarHandle.AccessMode.GET else VarHandle.AccessMode.SET
            )
        } else {
            val theMethod = theClass.methods.first {
                var parameterCount = it.parameterTypes.size
                if (!Modifier.isStatic(it.modifiers)) {
                    parameterCount += 1
                }

                return@first it.name == targetName && parameterCount == numArgs
            }
            if (theMethod == null) {
                SturdyPancake.LOGGER.error("unable to bind method $targetName on class $className")
                return MemorySegment.NULL
            }

            targetHandle = MethodHandles.lookup().unreflect(theMethod)
        }

        val upcallHandle = MethodHandles.lookup().findStatic(
            NativeCallbacks::class.java,
            "invokeMethodHandleWithNativeArguments",
            MethodType.methodType(
                Void.TYPE,
                MemorySegment::class.java,
                MethodHandle::class.java
            )
        )

        val boundHandle = MethodHandles.insertArguments(upcallHandle, 1, targetHandle)
        val callbackType = FunctionDescriptor.ofVoid(C_POINTER)

        val stub = Linker.nativeLinker().upcallStub(boundHandle, callbackType, Arena.ofAuto());
        allocatedBindings[funcName] = stub
        return stub
    }

    @JvmStatic
    fun invokeMethodHandleWithNativeArguments(arguments: MemorySegment, targetHandle: MethodHandle) {
        try {
            val javaArgs = mutableListOf<Any?>()
            repeat(Dart_GetNativeArgumentCount(arguments)) { idx ->
                javaArgs.add(
                    dartHandleToJavaObject(
                        Dart_GetNativeArgument(arguments, idx),
                        targetType = targetHandle.type().parameterType(idx)
                    )
                )
            }

            val result = targetHandle.invokeWithArguments(javaArgs)
            if (targetHandle.type().returnType() != Void.TYPE) {
                Dart_SetReturnValue(arguments, javaObjectToDartHandle(result))
            }
        } catch (e: Throwable) {
            SturdyPancake.LOGGER.error("failed to run method handle with native arguments", e)
            Arena.ofConfined().use { arena ->
                Dart_SetReturnValue(arguments, Dart_NewApiError(arena.allocateFrom(e.message)))
            }
        }
    }

    @JvmStatic
    fun setupBindingsResolver(libraryUris: List<String>) = Arena.ofConfined().use { arena ->
        for (libraryUri in libraryUris) {
            val library = Dart_LookupLibrary(Dart_NewStringFromCString(arena.allocateFrom(libraryUri)))
            if (Dart_IsError(library)) {
                throw IllegalArgumentException("unknown library: $libraryUri")
            }

            Dart_SetNativeResolver(library, SturdyPancake.dartEnvironment.bindingsResolver, MemorySegment.NULL)
        }
    }

    private val DART_LOGGER = LoggerFactory.getLogger("dartvm")

    @JvmStatic
    fun printToLogger(line: String) {
        DART_LOGGER.info(line)
    }

    @JvmStatic
    fun marshallAs(handle: KDartHandle, targetType: String): KDartHandle {
       Dart_SetPeer(handle.handle, Arena.global().allocateFrom(targetType))
       return handle
    }
}