import 'dart:ffi';

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

@pragma('vm:external-name', '::io.wispforest.sturdypancake.NativeCallbacks::printToLogger')
external void _printToLogger(String line);

@entryPoint
void _pancakePrint(String line) => _printToLogger(line);

// ---

@entryPoint
JavaObject _javaObjectForId(int id) => JavaObject._allocatedInstances[id] ??= JavaObject._(id);

@entryPoint
int _idOfJavaObject(JavaObject object) => object.id;

// ---

@entryPoint
Map<dynamic, dynamic> _mapFromLists(List<dynamic> keys, List<dynamic> values) {
  final result = {};
  for (final (idx, key) in keys.indexed) {
    result[key] = values[idx];
  }

  return result;
}
