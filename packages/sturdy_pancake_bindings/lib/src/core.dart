import 'dart:developer';
import 'dart:ffi';
import 'dart:isolate';

import 'package:vm_service/vm_service.dart' hide Isolate;
import 'package:vm_service/vm_service_io.dart';

const entryPoint = pragma('vm:entry-point');

@pragma('vm:external-name', '::io.wispforest.sturdypancake.NativeCallbacks::setupBindingsResolver')
external void setupBindingsResolver(List<String> libraryUris);

// ---

@pragma('vm:external-name', '::io.wispforest.sturdypancake.NativeCallbacks::setLambdaType')
external T lambda<T extends Function>(T fn, String targetType);

// ---

@entryPoint
@pragma('vm:isolate-unsendable')
final class JavaObject implements Finalizable {
  final int id;

  JavaObject._(this.id) {
    _finalizer.attach(this, Pointer.fromAddress(id));
    _increaseRefCount(id);
  }

  static final Map<int, JavaObject> _allocatedInstances = {};
  static final _finalizer = NativeFinalizer(Pointer.fromAddress(_fetchFinalizerPointer()).cast());

  @pragma('vm:external-name', '::io.wispforest.sturdypancake.JavaObjectTracker::fetchFinalizerPointer')
  external static int _fetchFinalizerPointer();

  @pragma('vm:external-name', '::io.wispforest.sturdypancake.JavaObjectTracker::increaseRefCount')
  external static void _increaseRefCount(int id);
}

// ---

extension type Logger._(JavaObject _) {
  @pragma('vm:external-name', '::org.slf4j.Logger::info')
  external void info(String message);
}

@pragma('vm:external-name', '::io.wispforest.sturdypancake.NativeCallbacks::getLogger')
external Logger getLogger(String name);

final _printLogger = getLogger("dart");

@entryPoint
void _pancakePrint(String line) => _printLogger.info(line);

// ---

@entryPoint
JavaObject _javaObjectForId(int id) => JavaObject._allocatedInstances[id] ??= JavaObject._(id);

@entryPoint
int _idOfJavaObject(JavaObject object) => object.id;

// ---

VmService? _vmServiceInstance;
Logger? _vmServiceLogger;

Future<void> _setupVmService() async {
  final uri = (await Service.getInfo()).serverWebSocketUri!;
  _vmServiceInstance = await vmServiceConnectUri(uri.toString());
  _vmServiceLogger = getLogger('dart-embedder');

  await _vmServiceInstance!.streamListen(EventStreams.kIsolate);
  _vmServiceInstance!.onIsolateEvent.listen((event) {
    if (event.kind != 'IsolateReload') return;

    _vmServiceLogger!.info('hot reload completed');
    _notifyReloadComplete();
  });
}

@entryPoint
void _requestHotReload() async {
  if (_vmServiceInstance == null) {
    await _setupVmService();
  }

  _vmServiceLogger!.info('requesting hot reload');
  _vmServiceInstance!.reloadSources(Service.getIsolateId(Isolate.current)!);
}

@pragma('vm:external-name', '::io.wispforest.sturdypancake.NativeCallbacks::notifyReloadComplete')
external void _notifyReloadComplete();

// ---

@entryPoint
Map<dynamic, dynamic> _mapFromLists(List<dynamic> keys, List<dynamic> values) {
  final result = {};
  for (final (idx, key) in keys.indexed) {
    result[key] = values[idx];
  }

  return result;
}
