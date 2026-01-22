package io.wispforest.sturdypancake

import io.wispforest.sturdypancake.bindings.DartBindings.*
import net.minecraft.client.Minecraft
import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemorySegment
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

object NativeCallbacks {
    @JvmStatic
    fun resolveExternalFunction(
        name: MemorySegment /* Dart_Handle */,
        numArgs: Int,
        autoSetupScope: Boolean
    ): MemorySegment /* Dart_NativeFunction */ {
        val funcName = dartStringToJavaString(name)
        if (!funcName.startsWith("method::")) {
            return MemorySegment.NULL
        }

        val (className, methodName) = funcName.substring("method::".length).split("::")

        val theClass: Class<*>
        try {
            theClass = Class.forName(className)
        } catch (_: ClassNotFoundException) {
            SturdyPancake.LOGGER.error("unable to bind class $className")
            return MemorySegment.NULL
        }

        val theMethod = theClass.methods.first { it.name == methodName }
        if (theMethod == null) {
            SturdyPancake.LOGGER.error("unable to bind method $methodName on class $className")
            return MemorySegment.NULL
        }

        val targetHandle = MethodHandles.lookup().unreflect(theMethod)
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

        return Linker.nativeLinker().upcallStub(boundHandle, callbackType, Arena.ofAuto())
    }

    @JvmStatic
    fun invokeMethodHandleWithNativeArguments(arguments: MemorySegment, targetHandle: MethodHandle) {
        val javaArgs = mutableListOf<Any>()
        repeat(Dart_GetNativeArgumentCount(arguments)) { idx ->
            javaArgs.add(dartHandleToJavaObject(Dart_GetNativeArgument(arguments, idx)))
        }

        val result = targetHandle.invokeWithArguments(javaArgs)
        if (targetHandle.type().returnType() != Void.TYPE) {
            Dart_SetReturnValue(arguments, javaObjectToDartHandle(result))
        }
    }
}