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

    lateinit var dartEnvironment: DartEnvironment
        private set

    val dartEnvironmentInitialized: Boolean
        get() = ::dartEnvironment.isInitialized

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
            DartDllConfig.isolate_setup_callback(config, upcallPointerFor(
                NativeCallbacks::class.java,
                "setupIsolate",
                Void.TYPE,
                listOf(),
                null,
                listOf()
            ))

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

            val bindingsResolver = upcallPointerFor(
                NativeCallbacks::class.java,
                "resolveJavaBinding",
                MemorySegment::class.java,
                listOf(MemorySegment::class.java, Int::class.java, MemorySegment::class.java),
                C_POINTER,
                listOf(C_POINTER, ValueLayout.JAVA_INT, C_POINTER)
            )

            Dart_EnterIsolate(isolate)
            Dart_EnterScope()

            val bindingsLibrary = Dart_NewPersistentHandle(Dart_LookupLibrary(Dart_NewStringFromCString(arena.allocateFrom("package:sturdy_pancake_bindings/src/core.dart"))))
            Dart_SetNativeResolver(bindingsLibrary, bindingsResolver, MemorySegment.NULL)

            dartEnvironment = DartEnvironment(
                bindingsLibrary,
                bindingsResolver
            )

            NativeCallbacks.setupIsolate()

            val entryPointResult = Dart_Invoke(
                Dart_RootLibrary(),
                Dart_NewStringFromCString(arena.allocateFrom("main")),
                0,
                MemorySegment.NULL
            )

            if (Dart_IsError(entryPointResult)) {
                val resultStr = Dart_GetError(entryPointResult).getString(0)
                LOGGER.error("failed to run entry point: $resultStr")
            }

            Dart_ExitScope()
//            Dart_ExitIsolate()
        }
    }
}