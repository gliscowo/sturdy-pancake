package io.wispforest.sturdypancake

import io.wispforest.sturdypancake.bindings.DartBindings.*
import java.lang.foreign.*
import java.lang.invoke.LambdaMetafactory
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.Method
import java.lang.reflect.Modifier

fun upcallPointerFor(
    clazz: Class<*>,
    methodName: String,
    javaReturnType: Class<*>,
    javaArgs: List<Class<*>>,
    nativeReturnType: MemoryLayout?,
    nativeArgs: List<MemoryLayout>
): MemorySegment {
    val methodHandle = MethodHandles.lookup().findStatic(
        clazz,
        methodName,
        MethodType.methodType(javaReturnType, javaArgs)
    )

    val descriptor = if (nativeReturnType != null) {
        FunctionDescriptor.of(nativeReturnType, *nativeArgs.toTypedArray())
    } else {
        FunctionDescriptor.ofVoid(*nativeArgs.toTypedArray())
    }

    return Linker.nativeLinker().upcallStub(methodHandle, descriptor, Arena.global())
}

fun dartStringToJavaString(dartString: MemorySegment /* Dart_Handle */): String = Arena.ofConfined().use { arena ->
    val resultCstr = arena.allocate(C_POINTER)
    Dart_StringToCString(dartString, resultCstr)

    return resultCstr.get(C_POINTER, 0).getString(0)
}

fun dartIntToJavaLong(dartInt: MemorySegment /* Dart_Handle */): Long = Arena.ofConfined().use { arena ->
    val result = arena.allocate(ValueLayout.JAVA_LONG)
    Dart_IntegerToInt64(dartInt, result)

    return result.get(ValueLayout.JAVA_LONG, 0)
}

fun dartDoubleToJavaDouble(dartDouble: MemorySegment /* Dart_Handle */): Double = Arena.ofConfined().use { arena ->
    val result = arena.allocate(ValueLayout.JAVA_DOUBLE)
    Dart_DoubleValue(dartDouble, result)

    return result.get(ValueLayout.JAVA_DOUBLE, 0)
}

fun dartBoolToJavaBoolean(dartBool: MemorySegment /* Dart_Handle */): Boolean = Arena.ofConfined().use { arena ->
    val result = arena.allocate(ValueLayout.JAVA_BOOLEAN)
    Dart_BooleanValue(dartBool, result)

    return result.get(ValueLayout.JAVA_BOOLEAN, 0)
}

var javaObjectTypeCache: MemorySegment? = null
private fun isJavaObjectHandle(handle: MemorySegment /* Dart_Handle */): Boolean = Arena.ofConfined().use { arena ->
    if (javaObjectTypeCache == null) {
        javaObjectTypeCache = Dart_NewPersistentHandle(
            Dart_GetClass(
                SturdyPancake.dartEnvironment.bindingsLibrary,
                Dart_NewStringFromCString(arena.allocateFrom("JavaObject"))
            )
        )
    }

    val isInstance = arena.allocate(ValueLayout.JAVA_BOOLEAN)
    Dart_ObjectIsType(handle, javaObjectTypeCache, isInstance)

    return isInstance.get(ValueLayout.JAVA_BOOLEAN, 0)
}

private fun Class<*>.isFunctionalInterface(): Boolean {
    return isInterface && methods.count { !Modifier.isStatic(it.modifiers) && Modifier.isAbstract(it.modifiers) } == 1
}

private fun Class<*>.getFunctionalInterfaceMethod(): Method {
    return methods.first { !Modifier.isStatic(it.modifiers) && Modifier.isAbstract(it.modifiers) }
}

data class KDartHandle(val pointer: MemorySegment /* Dart_Handle */)

private fun getPeer(dartHandle: MemorySegment): MemorySegment = Arena.ofConfined().use { arena ->
    val peerOut = arena.allocate(C_POINTER)
    Dart_GetPeer(dartHandle, peerOut)

    return peerOut.get(C_POINTER, 0)
}

fun dartHandleToJavaObject(dartHandle: MemorySegment /* Dart_Handle */, targetType: Class<*>? = null): Any? {
    if (Dart_IsNull(dartHandle)) {
        return null
    } else if (Dart_IsString(dartHandle)) {
        return dartStringToJavaString(dartHandle)
    } else if (Dart_IsInteger(dartHandle)) {
        return dartIntToJavaLong(dartHandle)
    } else if (Dart_IsDouble(dartHandle)) {
        return dartDoubleToJavaDouble(dartHandle)
    } else if (Dart_IsBoolean(dartHandle)) {
        return dartBoolToJavaBoolean(dartHandle)
    } else if (Dart_IsList(dartHandle)) {
        Arena.ofConfined().use { arena ->
            val length = arena.allocate(C_POINTER)
            Dart_ListLength(dartHandle, length)

            val result = mutableListOf<Any?>()
            repeat(length.get(ValueLayout.JAVA_LONG, 0).toInt()) { idx ->
                result.add(dartHandleToJavaObject(Dart_ListGetAt(dartHandle, idx.toLong())))
            }

            return result
        }
    } else if (Dart_IsMap(dartHandle)) {
        Arena.ofConfined().use { arena ->
            val keyList = Dart_MapKeys(dartHandle)
            val keyListLength = arena.allocate(C_POINTER)
            Dart_ListLength(keyList, keyListLength)

            val entries = mutableListOf<Pair<Any?, Any?>>()
            repeat(keyListLength.get(ValueLayout.JAVA_LONG, 0).toInt()) { idx ->
                val keyHandle = Dart_ListGetAt(dartHandle, idx.toLong())
                entries.add(
                    dartHandleToJavaObject(keyHandle) to dartHandleToJavaObject(
                        Dart_MapGetAt(dartHandle, keyHandle)
                    )
                )
            }

            return entries.toMap()
        }
    } else if (Dart_IsClosure(dartHandle) && ((targetType?.isFunctionalInterface()
            ?: false) || getPeer(dartHandle) != MemorySegment.NULL)
    ) {
        val targetClass: Class<*>

        val peer = getPeer(dartHandle)
        if (peer != MemorySegment.NULL) {
            targetClass = Class.forName(peer.getString(0))
        } else {
            targetClass = targetType!!
        }

        val targetMethod = targetClass.getFunctionalInterfaceMethod()
        val targetArgCount = targetMethod.parameterCount

        val argumentTypes = mutableListOf<Class<*>>(MemorySegment::class.java).let {
            it.addAll((0..<targetArgCount).map { Any::class.java })
            return@let it.toTypedArray()
        }

        val lookup = MethodHandles.lookup()
        val invokerHandle = lookup.findStatic(
            ClosureInvokers::class.java,
            "invoke$targetArgCount",
            MethodType.methodType(
                Any::class.java,
                argumentTypes
            )
        )

        val closureHandle = Dart_NewPersistentHandle(dartHandle)

        val function = LambdaMetafactory.metafactory(
            lookup,
            targetMethod.name,
            MethodType.methodType(targetClass, MemorySegment::class.java),
            MethodType.methodType(targetMethod.returnType, targetMethod.parameterTypes),
            invokerHandle,
            MethodType.methodType(targetMethod.returnType, targetMethod.parameterTypes)
        ).target.invoke(closureHandle)

        return function
    } else if (targetType == KDartHandle::class.java) {
        return KDartHandle(dartHandle)
    } else if (isJavaObjectHandle(dartHandle)) {
        Arena.ofConfined().use { arena ->
            val args = arena.allocate(C_POINTER, 1)
            args.setAtIndex(C_POINTER, 0, dartHandle)

            Dart_EnterScope()
            val javaObjectIdHandle = Dart_Invoke(
                SturdyPancake.dartEnvironment.bindingsLibrary,
                Dart_NewStringFromCString(arena.allocateFrom("_idOfJavaObject")),
                1,
                args
            )
            Dart_ExitScope()

            return JavaObjectTracker.objectForId(dartIntToJavaLong(javaObjectIdHandle))
        }
    } else if (Dart_IsError(dartHandle)) {
        throw UnsupportedOperationException("dart error: ${Dart_GetError(dartHandle).getString(0)}")
    } else {
        throw UnsupportedOperationException("cannot convert given dart handle to a java object")
    }
}

fun javaObjectToDartHandle(javaObject: Any?): MemorySegment /* Dart_Handle */ {
    when (javaObject) {
        null -> {
            return Dart_Null()
        }

        is String -> Arena.ofConfined().use { arena ->
            return Dart_NewStringFromCString(arena.allocateFrom(javaObject))
        }

        is Byte, is Short, is Int, is Long -> {
            val longValue = javaObject.toLong()
            return Dart_NewInteger(longValue)
        }

        is Float, is Double -> {
            val doubleValue = javaObject.toDouble()
            return Dart_NewDouble(doubleValue)
        }

        is Boolean -> {
            return Dart_NewBoolean(javaObject)
        }

        is List<*> -> {
            val listHandle = Dart_NewList(javaObject.size.toLong())
            javaObject.forEachIndexed { index, element ->
                Dart_ListSetAt(listHandle, index.toLong(), javaObjectToDartHandle(element))
            }

            return listHandle
        }

        is Map<*, *> -> Arena.ofConfined().use { arena ->
            val keys = Dart_NewList(javaObject.size.toLong())
            val values = Dart_NewList(javaObject.size.toLong())

            var mapIdx: Long = 0
            javaObject.forEach { (k, v) ->
                Dart_ListSetAt(keys, mapIdx, javaObjectToDartHandle(k))
                Dart_ListSetAt(values, mapIdx, javaObjectToDartHandle(v))

                mapIdx++
            }

            val mapConstructorArgs = arena.allocate(C_POINTER, 2)
            mapConstructorArgs.setAtIndex(C_POINTER, 0, keys)
            mapConstructorArgs.setAtIndex(C_POINTER, 1, values)

            val mapHandle = Dart_Invoke(
                SturdyPancake.dartEnvironment.bindingsLibrary,
                Dart_NewStringFromCString(arena.allocateFrom("_mapFromLists")),
                2,
                mapConstructorArgs
            )

            return mapHandle
        }

        is KDartHandle -> return javaObject.pointer

        else -> Arena.ofConfined().use { arena ->
            val objectId = JavaObjectTracker.objectIdOf(javaObject)

            val args = arena.allocate(C_POINTER, 1)
            args.setAtIndex(C_POINTER, 0, Dart_NewInteger(objectId))

            val javaObjectHandle = Dart_Invoke(
                SturdyPancake.dartEnvironment.bindingsLibrary,
                Dart_NewStringFromCString(arena.allocateFrom("_javaObjectForId")),
                1,
                args
            )

            return javaObjectHandle
        }
    }
}