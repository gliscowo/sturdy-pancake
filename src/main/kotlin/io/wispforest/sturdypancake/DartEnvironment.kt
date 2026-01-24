package io.wispforest.sturdypancake

import java.lang.foreign.MemorySegment

data class DartEnvironment(
    val bindingsLibrary: MemorySegment,
    val bindingsResolver: MemorySegment
)
