import 'dart:ffi';

const entryPoint = pragma('vm:entry-point');

@pragma('vm:external-name', '::io.wispforest.sturdypancake.NativeCallbacks::setupBindingsResolver')
external void setupBindingsResolver(List<String> libraryUris);

// ---

extension Marshalling<T extends Function> on T {
  @pragma('vm:external-name', '::io.wispforest.sturdypancake.NativeCallbacks::marshallAs')
  external T marshallAs(String targetType);
}

// ---

@entryPoint
class JavaObject implements Finalizable {
  final int id;

  JavaObject(this.id) {
    finalizer.attach(this, Pointer.fromAddress(id));
  }

  static final Map<int, JavaObject> _allocatedInstances = {};
  static final finalizer = NativeFinalizer(Pointer.fromAddress(_fetchFinalizerPointer()).cast());

  @pragma('vm:external-name', '::io.wispforest.sturdypancake.JavaObjectTracker::fetchFinalizerPointer')
  external static int _fetchFinalizerPointer();
}

// ---

@pragma('vm:external-name', '::io.wispforest.sturdypancake.NativeCallbacks::printToLogger')
external void _printToLogger(String line);

@entryPoint
void _pancakePrint(String line) => _printToLogger(line);

// ---

@entryPoint
JavaObject _javaObjectForId(int id) => JavaObject._allocatedInstances[id] ??= JavaObject(id);

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
