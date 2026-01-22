package io.wispforest.sturdypancake

import io.wispforest.sturdypancake.bindings.DartBindings.*
import io.wispforest.sturdypancake.bindings.DartDllConfig
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import java.nio.file.Files
import kotlin.io.path.absolute
import kotlin.io.path.pathString

object SturdyPancake : ModInitializer {

    val LOGGER: Logger = LoggerFactory.getLogger("sturdy-pancake")

    private var bindingsLibraryField: MemorySegment? = null
    val bindingsLibrary: MemorySegment
        get() = bindingsLibraryField!!

    private var bindingsResolverField: MemorySegment? = null
    val bindingsResolver: MemorySegment
        get() = bindingsResolverField!!

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

            val isolate = DartDll_LoadScript(
                arena.allocateFrom("/home/lily/IdeaProjects/sturdy-pancake/packages/impl_test/bin/impl_test.dart"),
                arena.allocateFrom("/home/lily/IdeaProjects/sturdy-pancake/packages/impl_test/.dart_tool/package_config.json"),
                MemorySegment.NULL
            )

            if (isolate == MemorySegment.NULL) {
                throw IllegalStateException("failed to load main isolate")
            }

            Dart_EnterIsolate(isolate)
            Dart_EnterScope()

            bindingsResolverField = upcallPointerFor(
                NativeCallbacks::class.java,
                "resolveJavaBinding",
                MemorySegment::class.java,
                listOf(MemorySegment::class.java, Int::class.java, MemorySegment::class.java),
                C_POINTER,
                listOf(C_POINTER, ValueLayout.JAVA_INT, C_POINTER)
            )

            val bindingsLibrary = Dart_NewPersistentHandle(Dart_LookupLibrary(Dart_NewStringFromCString(arena.allocateFrom("package:sturdy_pancake_bindings/src/core.dart"))))
            bindingsLibraryField = bindingsLibrary

            Dart_SetNativeResolver(bindingsLibrary, bindingsResolver, MemorySegment.NULL)

            val builtinLibrary = Dart_LookupLibrary(Dart_NewStringFromCString(arena.allocateFrom("dart:_internal")))
            val printClosure = Dart_GetField(SturdyPancake.bindingsLibrary, Dart_NewStringFromCString(arena.allocateFrom("_pancakePrint")))

            Dart_SetField(
                builtinLibrary,
                Dart_NewStringFromCString(arena.allocateFrom("_printClosure")),
                printClosure
            )

            val mainResult = Dart_Invoke(
                Dart_RootLibrary(),
                Dart_NewStringFromCString(arena.allocateFrom("main")),
                0,
                MemorySegment.NULL
            )

            if (Dart_IsError(mainResult)) {
                val resultStr = Dart_GetError(mainResult).getString(0)
                LOGGER.error("failed to run main: $resultStr")
            } else {
                val resultCstr = arena.allocate(C_POINTER)

                val toStringResult = Dart_StringToCString(Dart_ToString(mainResult), resultCstr)
                if (Dart_IsError(toStringResult)) {
                    LOGGER.error("failed to convert main result to a string: ${Dart_GetError(toStringResult).getString(0)}")
                } else {
                    LOGGER.info("main result: ${resultCstr.get(C_POINTER, 0).getString(0)}")
                }
            }

            Dart_ExitScope()
//            Dart_ExitIsolate()
        }
    }
}