package io.wispforest.sturdypancake

import java.lang.foreign.ValueLayout
import java.util.*
import kotlin.random.Random

object JavaObjectTracker {

    private val idToObject: MutableMap<Long, Any> = mutableMapOf()
    private val objectToId: MutableMap<Any, Long> = IdentityHashMap()

    private val idToRefCount: MutableMap<Long, Long> = mutableMapOf()

    @JvmStatic
    @Synchronized
    fun objectIdOf(theObject: Any): Long {
        if (objectToId.contains(theObject)) {
            return objectToId[theObject]!!
        }

        var newId: Long
        do {
            newId = Random.nextLong()
        } while (idToObject.contains(newId))

        idToObject[newId] = theObject;
        objectToId[theObject] = newId;
        idToRefCount[newId] = 0
        return newId;
    }

    @JvmStatic
    @Synchronized
    fun objectForId(objectId: Long): Any = idToObject[objectId]!!

    @JvmStatic
    @Synchronized
    fun increaseRefCount(id: Long) {
        idToRefCount[id] = idToRefCount[id]!! + 1
    }

    @JvmStatic
    @Synchronized
    fun decreaseRefCount(id: Long) {
        val newRefCount = idToRefCount[id]!! - 1
        idToRefCount[id] = newRefCount

        if (newRefCount == 0L) {
            val theObject = idToObject.remove(id)!!
            objectToId.remove(theObject)
            idToRefCount.remove(id)
        }
    }

    @JvmStatic
    fun fetchFinalizerPointer(): Long = upcallPointerFor(
        JavaObjectTracker::class.java,
        "decreaseRefCount",
        Void.TYPE,
        listOf(Long::class.java),
        null,
        listOf(ValueLayout.JAVA_LONG)
    ).address()
}