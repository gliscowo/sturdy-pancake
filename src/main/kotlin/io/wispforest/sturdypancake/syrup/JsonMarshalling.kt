package io.wispforest.sturdypancake.syrup

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import io.wispforest.sturdypancake.DartSymbols
import io.wispforest.sturdypancake.SturdyPancake
import io.wispforest.sturdypancake.bindings.DartBindings.*
import io.wispforest.sturdypancake.toDartHandle
import io.wispforest.sturdypancake.tryConvertToJavaInstance
import java.lang.foreign.MemorySegment

fun jsonToDartObject(json: JsonElement): MemorySegment /* Dart_Handle */ {
    if (json.isJsonArray) {
        val dartList = Dart_NewList(json.asJsonArray.size().toLong())
        for ((idx, element) in json.asJsonArray.withIndex()) {
            Dart_ListSetAt(dartList, idx.toLong(), jsonToDartObject(element))
        }

        return dartList
    } else if (json.isJsonObject) {
        val dartKeysList = Dart_NewList(json.asJsonObject.size().toLong())
        val dartValuesList = Dart_NewList(json.asJsonObject.size().toLong())

        for ((idx, entry) in json.asJsonObject.asMap().entries.withIndex()) {
            Dart_ListSetAt(dartKeysList, idx.toLong(), entry.key.toDartHandle())
            Dart_ListSetAt(dartValuesList, idx.toLong(), jsonToDartObject(entry.value))
        }

        return SturdyPancake.dartEnvironment.invoke(
            DartSymbols.Bindings.JSON_MAP_FROM_LISTS,
            dartKeysList,
            dartValuesList
        )
    } else if (json.isJsonPrimitive) {
        val primitive = json.asJsonPrimitive
        return if (primitive.isBoolean) {
            primitive.asBoolean.toDartHandle()
        } else if (primitive.isNumber) {
            primitive.asNumber.toDouble().toDartHandle()
        } else {
            primitive.asString.toDartHandle()
        }
    } else {
        return Dart_Null()
    }
}

fun dartObjectToJson(dartObject: MemorySegment /* Dart_Handle */): JsonElement {
    val javaObject = tryConvertToJavaInstance(dartObject)
    return javaObjectToJson(javaObject)
}

private fun javaObjectToJson(javaObject: Any?): JsonElement {
    when (javaObject) {
        is List<*> -> {
            val jsonArray = JsonArray()
            for (element in javaObject) {
                jsonArray.add(javaObjectToJson(element))
            }

            return jsonArray
        }

        is Map<*, *> -> {
            val jsonObject = JsonObject()
            javaObject.forEach { (key, value) ->
                jsonObject.add(key as String, javaObjectToJson(value))
            }

            return jsonObject
        }

        is Boolean -> {
            return JsonPrimitive(javaObject)
        }

        is Number -> {
            return JsonPrimitive(javaObject)
        }

        is String -> {
            return JsonPrimitive(javaObject)
        }

        null -> {
            return JsonNull.INSTANCE
        }

        else -> {
            throw IllegalStateException("invalid dart json object")
        }
    }
}