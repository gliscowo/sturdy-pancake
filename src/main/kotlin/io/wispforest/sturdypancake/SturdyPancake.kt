package io.wispforest.sturdypancake

import io.wispforest.sturdypancake.bindings.DartBindings.*
import io.wispforest.sturdypancake.bindings.DartDllConfig
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.nio.file.Files
import kotlin.io.path.absolute
import kotlin.io.path.pathString

object SturdyPancake : ModInitializer {

    val LOGGER: Logger = LoggerFactory.getLogger("sturdy-pancake")

    private var rootLibraryField: MemorySegment? = null
    val rootLibrary: MemorySegment
        get() = rootLibraryField!!

    fun setupNatives() {
        val nativesDir = FabricLoader.getInstance().gameDir.resolve("sturdy_natives")
        if (!Files.isDirectory(nativesDir)) {
            Files.createDirectory(nativesDir)
        }

        val dylibPath = nativesDir.resolve("libdart_dll.so")
        if (!Files.isRegularFile(dylibPath)) {
            LOGGER.info("installed shared library")

            val dylibAsset =
                FabricLoader.getInstance().getModContainer("sturdy-pancake").get().findPath("libdart_dll.so").get()
            Files.copy(dylibAsset, dylibPath)
        }

        System.load(dylibPath.absolute().pathString)
    }

    override fun onInitialize() {
        setupNatives()

        Arena.ofConfined().use { arena ->
            val config = DartDllConfig.allocate(arena)
            DartDllConfig.start_service_isolate(config, false)
            DartDllConfig.service_port(config, 6767)

            if (!DartDll_Initialize(config)) {
                throw IllegalStateException("failed to initialize dart")
            }

            val mainPath = FabricLoader.getInstance().gameDir.resolve("sturdy_pancake.dart")
            val mainPathCstr = arena.allocateFrom(mainPath.absolute().pathString)

            val isolate = DartDll_LoadScript(mainPathCstr, MemorySegment.NULL, MemorySegment.NULL)
            if (isolate == MemorySegment.NULL) {
                throw IllegalStateException("failed to load main isolate")
            }

            Dart_EnterIsolate(isolate)
            Dart_EnterScope()

            val mainLibrary = Dart_RootLibrary()
            rootLibraryField = mainLibrary

            val nativeResolver = upcallPointerFor(
                NativeCallbacks::class.java,
                "resolveExternalFunction",
                MemorySegment::class.java,
                listOf(MemorySegment::class.java, Int::class.java, Boolean::class.java),
                C_POINTER,
                listOf(C_POINTER, ValueLayout.JAVA_INT, ValueLayout.JAVA_BOOLEAN)
            )

            val allLibraries = Dart_GetLoadedLibraries()
            val libraryCount = arena.allocate(ValueLayout.JAVA_LONG)
            Dart_ListLength(allLibraries, libraryCount)

            repeat(libraryCount.get(ValueLayout.JAVA_LONG, 0).toInt()) { idx ->
                val library = Dart_ListGetAt(allLibraries, idx.toLong())
                println(dartStringToJavaString(Dart_LibraryUrl(library)))
                if (dartStringToJavaString(Dart_LibraryUrl(library)).startsWith("dart:")) {
                    return@repeat
                }

                Dart_SetNativeResolver(library, nativeResolver, MemorySegment.NULL)
            }

            val mainResult = Dart_Invoke(
                mainLibrary,
                Dart_NewStringFromCString(arena.allocateFrom("main")),
                0,
                MemorySegment.NULL
            )

            if (Dart_IsError(mainResult)) {
                val resultStr = Dart_GetError(mainResult).getString(0)
                LOGGER.error("failed to run main: $resultStr")
            } else {
                val resultCstr = arena.allocate(C_POINTER)
                Dart_StringToCString(Dart_ToString(mainResult), resultCstr)

                LOGGER.info("main result: ${resultCstr.get(C_POINTER.withTargetLayout(C_POINTER), 0).getString(0)}")
            }

            Dart_ExitScope()
            Dart_ExitIsolate()
        }
    }
}