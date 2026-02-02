package io.wispforest.sturdypancake

object DartSymbols {
    const val INTERNAL_LIBRARY_URI = "dart:_internal"
    const val PRINT_CLOSURE_FIELD = "_printClosure"

    object Bindings {
        const val JAVA_OBJECT_TYPE_NAME = "JavaObject"
        const val JAVA_OBJECT_FOR_ID = "_javaObjectForId"
        const val ID_OF_JAVA_OBJECT = "_idOfJavaObject"

        const val MAP_FROM_LISTS = "_mapFromLists"
        const val JSON_MAP_FROM_LISTS = "_jsonMapFromLists"
        const val PRINT_TO_LOGGER = "_printToLogger"
        const val REQUEST_HOT_RELOAD = "_requestHotReload"
    }
}