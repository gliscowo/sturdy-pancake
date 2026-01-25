package io.wispforest.sturdypancake

import io.wispforest.owo.Owo
import io.wispforest.sturdypancake.bindings.DartBindings.*
import java.lang.foreign.*
import java.lang.invoke.LambdaMetafactory
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.Modifier
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

fun upcallPointerFor(
    method: KFunction<*>,
    nativeReturnType: MemoryLayout?,
    nativeArgs: List<MemoryLayout>
): MemorySegment {
    val methodHandle = MethodHandles.lookup().findStatic(
        method.javaMethod!!.declaringClass,
        method.name,
        MethodType.methodType(method.javaMethod!!.returnType, method.javaMethod!!.parameterTypes)
    )

    val descriptor = if (nativeReturnType != null) {
        FunctionDescriptor.of(nativeReturnType, *nativeArgs.toTypedArray())
    } else {
        FunctionDescriptor.ofVoid(*nativeArgs.toTypedArray())
    }

    return Linker.nativeLinker().upcallStub(methodHandle, descriptor, Arena.global())
}

// ---

fun String.toDartHandle(): MemorySegment = Arena.ofConfined().use { arena ->
    return Dart_NewStringFromCString(arena.allocateFrom(this))
}

fun MemorySegment.getDartString(): String = Arena.ofConfined().use { arena ->
    if (Owo.DEBUG) {
        check(Dart_IsString(this)) { "tried to get a string from a non-string handle" }
    }

    val resultCstr = arena.allocate(C_POINTER)
    Dart_StringToCString(this, resultCstr)

    return resultCstr.get(C_POINTER, 0).getString(0)
}

fun Long.toDartHandle(): MemorySegment = Dart_NewInteger(this)

fun MemorySegment.getDartInt(): Long = Arena.ofConfined().use { arena ->
    if (Owo.DEBUG) {
        check(Dart_IsInteger(this)) { "tried to get a int from a non-int handle" }
    }

    val result = arena.allocate(ValueLayout.JAVA_LONG)
    Dart_IntegerToInt64(this, result)

    return result.get(ValueLayout.JAVA_LONG, 0)
}

fun Double.toDartHandle(): MemorySegment = Dart_NewDouble(this)

fun MemorySegment.getDartDouble(): Double = Arena.ofConfined().use { arena ->
    if (Owo.DEBUG) {
        check(Dart_IsDouble(this)) { "tried to get a double from a non-double handle" }
    }

    val result = arena.allocate(ValueLayout.JAVA_DOUBLE)
    Dart_DoubleValue(this, result)

    return result.get(ValueLayout.JAVA_DOUBLE, 0)
}

fun Boolean.toDartHandle(): MemorySegment = Dart_NewBoolean(this)

fun MemorySegment.getDartBool(): Boolean = Arena.ofConfined().use { arena ->
    if (Owo.DEBUG) {
        check(Dart_IsBoolean(this)) { "tried to get a boolean from a non-bool handle" }
    }

    val result = arena.allocate(ValueLayout.JAVA_BOOLEAN)
    Dart_BooleanValue(this, result)

    return result.get(ValueLayout.JAVA_BOOLEAN, 0)
}

fun List<*>.toDartHandle(): MemorySegment {
    val listHandle = Dart_NewList(size.toLong())
    forEachIndexed { index, element ->
        Dart_ListSetAt(listHandle, index.toLong(), tryConvertToDartInstance(element))
    }

    return listHandle
}

fun MemorySegment.getDartList(): List<Any?> = Arena.ofConfined().use { arena ->
    if (Owo.DEBUG) {
        check(Dart_IsList(this)) { "tried to get a list from a non-list handle" }
    }

    val length = arena.allocate(C_POINTER)
    Dart_ListLength(this, length)

    val result = mutableListOf<Any?>()
    repeat(length.get(ValueLayout.JAVA_LONG, 0).toInt()) { idx ->
        result.add(tryConvertToJavaInstance(Dart_ListGetAt(this, idx.toLong())))
    }

    return result
}

fun Map<*, *>.toDartHandle(): MemorySegment {
    val keys = Dart_NewList(size.toLong())
    val values = Dart_NewList(size.toLong())

    var mapIdx: Long = 0
    forEach { (k, v) ->
        Dart_ListSetAt(keys, mapIdx, tryConvertToDartInstance(k))
        Dart_ListSetAt(values, mapIdx, tryConvertToDartInstance(v))

        mapIdx++
    }

    val mapHandle = SturdyPancake.dartEnvironment.invoke(
        DartSymbols.Bindings.MAP_FROM_LISTS,
        keys,
        values
    )

    return mapHandle
}

fun MemorySegment.getDartMap(): Map<Any?, Any?> = Arena.ofConfined().use { arena ->
    if (Owo.DEBUG) {
        check(Dart_IsMap(this)) { "tried to get a map from a non-map handle" }
    }

    val keyList = Dart_MapKeys(this)
    val keyListLength = arena.allocate(C_POINTER)
    Dart_ListLength(keyList, keyListLength)

    val entries = mutableListOf<Pair<Any?, Any?>>()
    repeat(keyListLength.get(ValueLayout.JAVA_LONG, 0).toInt()) { idx ->
        val keyHandle = Dart_ListGetAt(this, idx.toLong())
        entries.add(
            tryConvertToJavaInstance(keyHandle) to tryConvertToJavaInstance(
                Dart_MapGetAt(this, keyHandle)
            )
        )
    }

    return entries.toMap()
}

// ---

data class KDartHandle(val pointer: MemorySegment /* Dart_Handle */)

// ---

private val javaObjectTypeCache: MemorySegment by lazy {
    Dart_NewPersistentHandle(
        Dart_GetClass(
            SturdyPancake.dartEnvironment.bindingsLibrary,
            DartSymbols.Bindings.JAVA_OBJECT_TYPE_NAME.toDartHandle()
        )
    )
}

private fun isJavaObjectHandle(handle: MemorySegment /* Dart_Handle */): Boolean = Arena.ofConfined().use { arena ->
    val isInstance = arena.allocate(ValueLayout.JAVA_BOOLEAN)
    Dart_ObjectIsType(handle, javaObjectTypeCache, isInstance)

    return isInstance.get(ValueLayout.JAVA_BOOLEAN, 0)
}

fun Any.toDartJavaObjectHandle(): MemorySegment {
    val objectId = JavaObjectTracker.objectIdOf(this)

    val javaObjectHandle = SturdyPancake.dartEnvironment.invoke(
        DartSymbols.Bindings.JAVA_OBJECT_FOR_ID,
        tryConvertToDartInstance(objectId)
    )

    return javaObjectHandle
}

fun MemorySegment.getJavaObject(): Any {
    val javaObjectIdHandle = SturdyPancake.dartEnvironment.invoke(
        DartSymbols.Bindings.ID_OF_JAVA_OBJECT,
        this
    ).getDartInt()

    return JavaObjectTracker.objectForId(javaObjectIdHandle)
}

// ---

private fun getPeer(dartHandle: MemorySegment): MemorySegment = Arena.ofConfined().use { arena ->
    val peerOut = arena.allocate(C_POINTER)
    Dart_GetPeer(dartHandle, peerOut)

    return peerOut.get(C_POINTER, 0)
}

private fun isLambdafiableClosure(dartHandle: MemorySegment, targetType: Class<*>?): Boolean {
    if (!Dart_IsClosure(dartHandle)) {
        return false
    }

    if (getPeer(dartHandle) != MemorySegment.NULL) {
        return true
    }

    val hasFunctionalInterfaceTarget = targetType != null
            && targetType.isInterface
            && targetType.methods.count { !Modifier.isStatic(it.modifiers) && Modifier.isAbstract(it.modifiers) } == 1

    return hasFunctionalInterfaceTarget
}

fun <T> MemorySegment.getDartClosure(lambdaType: Class<T>): T {
    val targetMethod = lambdaType.methods.first {
        !Modifier.isStatic(it.modifiers) && Modifier.isAbstract(it.modifiers)
    }
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

    val closureHandle = Dart_NewPersistentHandle(this)

    @Suppress("UNCHECKED_CAST")
    val function = LambdaMetafactory.metafactory(
        lookup,
        targetMethod.name,
        MethodType.methodType(lambdaType, MemorySegment::class.java),
        MethodType.methodType(targetMethod.returnType, targetMethod.parameterTypes),
        invokerHandle,
        MethodType.methodType(targetMethod.returnType, targetMethod.parameterTypes)
    ).target.invoke(closureHandle) as T

    return function
}

// ---

fun tryConvertToJavaInstance(dartHandle: MemorySegment /* Dart_Handle */, targetType: Class<*>? = null): Any? {
    when {
        Dart_IsNull(dartHandle) -> {
            return null
        }

        Dart_IsString(dartHandle) -> {
            return dartHandle.getDartString()
        }

        Dart_IsInteger(dartHandle) -> {
            return dartHandle.getDartInt()
        }

        Dart_IsDouble(dartHandle) -> {
            return dartHandle.getDartDouble()
        }

        Dart_IsBoolean(dartHandle) -> {
            return dartHandle.getDartBool()
        }

        Dart_IsList(dartHandle) -> {
            return dartHandle.getDartList()
        }

        Dart_IsMap(dartHandle) -> {
            return dartHandle.getDartMap()
        }

        isLambdafiableClosure(dartHandle, targetType) -> {
            val lambdaType: Class<*>

            val peer = getPeer(dartHandle)
            if (peer != MemorySegment.NULL) {
                lambdaType = Class.forName(peer.getString(0))
            } else {
                lambdaType = targetType!!
            }

            return dartHandle.getDartClosure(lambdaType)
        }

        targetType == KDartHandle::class.java -> {
            return KDartHandle(dartHandle)
        }

        isJavaObjectHandle(dartHandle) -> {
            return dartHandle.getJavaObject()
        }

        Dart_IsError(dartHandle) -> {
            throw UnsupportedOperationException("dart error: ${Dart_GetError(dartHandle).getString(0)}")
        }

        else -> {
            val isInstance = Dart_IsInstance(dartHandle)
            if (!isInstance) {
                throw UnsupportedOperationException("tryConvertToJavaInstance: dartHandle is not a valid dart instance")
            }

            val instanceToString = Dart_ToString(dartHandle)
            if (Dart_IsError(instanceToString)) {
                val error = Dart_GetError(instanceToString).getString(0)
                throw UnsupportedOperationException("tryConvertToJavaInstance: dartHandle is not a of recognized type. toString() error: $error")
            }

            val instanceTypeName = Dart_ClassName(Dart_InstanceGetType(dartHandle))
            if (Dart_IsError(instanceTypeName)) {
                throw UnsupportedOperationException(
                    "tryConvertToJavaInstance: dartHandle is not of a recognized type. toString(): ${instanceToString.getDartString()}"
                )
            }

            throw UnsupportedOperationException(
                "tryConvertToJavaInstance: dartHandle is of unrecognized type ${instanceTypeName.getDartString()}. toString(): ${instanceToString.getDartString()}"
            )
        }
    }
}

fun tryConvertToDartInstance(javaObject: Any?): MemorySegment /* Dart_Handle */ {
    when (javaObject) {
        null -> {
            return Dart_Null()
        }

        is String -> {
            return javaObject.toDartHandle()
        }

        is Byte, is Short, is Int, is Long -> {
            return javaObject.toLong().toDartHandle()
        }

        is Float, is Double -> {
            return javaObject.toDouble().toDartHandle()
        }

        is Boolean -> {
            return javaObject.toDartHandle()
        }

        is List<*> -> {
            return javaObject.toDartHandle()
        }

        is Map<*, *> -> {
            return javaObject.toDartHandle()
        }

        is KDartHandle -> {
            return javaObject.pointer
        }

        else -> {
            return javaObject.toDartJavaObjectHandle()
        }
    }
}