package io.wispforest.sturdypancake

import java.lang.foreign.ValueLayout
import java.util.*
import kotlin.random.Random

object JavaObjectTracker {

    private val idToObject: MutableMap<Long, Any> = mutableMapOf()
    private val objectToId: MutableMap<Any, Long> = IdentityHashMap()

    fun objectIdOf(theObject: Any): Long {
        if (objectToId.contains(theObject)) {
            return objectToId[theObject]!!
        }

        var newId: Long
        do {
            newId = Random.nextLong()
        } while (idToObject.contains(newId));

        idToObject[newId] = theObject;
        objectToId[theObject] = newId;
        return newId;
    }

    fun objectForId(objectId: Long): Any = idToObject[objectId]!!

    @JvmStatic
    @Synchronized
    fun finalizeObject(id: Long) {
        val theObject = idToObject.remove(id)!!
        objectToId.remove(theObject)
    }

    @JvmStatic
    fun fetchFinalizerPointer(): Long = upcallPointerFor(
        JavaObjectTracker::class.java,
        "finalizeObject",
        Void.TYPE,
        listOf(Long::class.java),
        null,
        listOf(ValueLayout.JAVA_LONG)
    ).address()
}