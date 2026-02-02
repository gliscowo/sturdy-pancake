package io.wispforest.sturdypancake

import io.wispforest.owo.ui.event.ClientRenderCallback
import io.wispforest.sturdypancake.bindings.DartBindings.*
import io.wispforest.sturdypancake.bindings.DartDllConfig
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
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

    const val MOD_ID = "sturdy-pancake"
    val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

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
                FabricLoader.getInstance().getModContainer(MOD_ID).get().findPath("libdart_dll.so").get()
            Files.copy(dylibAsset, dylibPath)
        }

        System.load(dylibPath.absolute().pathString)
    }

    override fun onInitialize() {
        setupNatives()

        Arena.ofConfined().use { arena ->
            val config = DartDllConfig.allocate(arena)
            DartDllConfig.start_service_isolate(config, true)
            DartDllConfig.service_port(config, 6767)
            DartDllConfig.isolate_setup_callback(
                config,
                upcallPointerFor(
                    NativeCallbacks::setupIsolate,
                    null,
                    listOf()
                )
            )

            if (!DartDll_Initialize(config)) {
                throw IllegalStateException("failed to initialize dart")
            }

            val mainIsolate = DartDll_LoadScript(
                arena.allocateFrom("/home/lily/IdeaProjects/sturdy-pancake/packages/impl_test/bin/impl_test.dart"),
                arena.allocateFrom("/home/lily/IdeaProjects/sturdy-pancake/packages/impl_test/.dart_tool/package_config.json"),
                MemorySegment.NULL
            )

            if (mainIsolate == MemorySegment.NULL) {
                throw IllegalStateException("failed to load main isolate")
            }

            val bindingsResolver = upcallPointerFor(
                NativeCallbacks::resolveJavaBinding,
                C_POINTER,
                listOf(C_POINTER, ValueLayout.JAVA_INT, C_POINTER)
            )

            Dart_EnterIsolate(mainIsolate)
            Dart_EnterScope()

            Dart_SetMessageNotifyCallback(
                upcallPointerFor(
                    NativeCallbacks::newIsolateMessage,
                    null,
                    listOf(C_POINTER)
                )
            )

            val bindingsLibrary =
                Dart_NewPersistentHandle(Dart_LookupLibrary(Dart_NewStringFromCString(arena.allocateFrom("package:sturdy_pancake_bindings/sturdy_pancake_bindings.dart"))))
            Dart_SetNativeResolver(bindingsLibrary, bindingsResolver, MemorySegment.NULL)

            dartEnvironment = DartEnvironment(
                mainIsolate,
                bindingsLibrary,
                bindingsResolver
            )

            NativeCallbacks.setupIsolate()

            val entryPointResult = dartEnvironment.invoke("main", target = Dart_RootLibrary())
            if (Dart_IsError(entryPointResult)) {
                val resultStr = Dart_GetError(entryPointResult).getString(0)
                LOGGER.error("failed to run entry point: $resultStr")
            }

            dartEnvironment.processOutstandingMainIsolateMessages()

            Dart_ExitScope()
            Dart_ExitIsolate()
        }

        ClientRenderCallback.BEFORE_SWAP.register {
            if (!dartEnvironment.mainIsolateHasOutstandingMessages) {
                return@register
            }

            dartEnvironment.runInMainIsolate {
                processOutstandingMainIsolateMessages()
            }
        }

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(ClientCommandManager.literal("reload_dart_sources").executes {
                dartEnvironment.runInMainIsolate {
                    invoke(DartSymbols.Bindings.REQUEST_HOT_RELOAD)
                }

                return@executes 0
            })
        }
    }
}