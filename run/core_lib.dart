import 'dart:ffi';

@pragma('vm:entry-point')
class JavaObject implements Finalizable {
  final int id;
  JavaObject(this.id) {
    finalizer.attach(this, Pointer.fromAddress(id));
  }

  static final Map<int, JavaObject> _allocatedInstances = {};
  static final finalizer = NativeFinalizer(Pointer.fromAddress(_fetchFinalizerPointer()).cast());

  @pragma('vm:external-name', 'method::io.wispforest.sturdypancake.JavaObjectTracker::fetchFinalizerPointer')
  external static int _fetchFinalizerPointer();
}

// ---

@pragma('vm:entry-point')
JavaObject __javaObjectForId(int id) => JavaObject._allocatedInstances[id] ??= JavaObject(id);

@pragma('vm:entry-point')
int __idOfJavaObject(JavaObject object) => object.id;

// ---------------

@pragma('vm:entry-point')
Map<dynamic, dynamic> __mapFromLists(List<dynamic> keys, List<dynamic> values) {
  final result = {};
  for (final (idx, key) in keys.indexed) {
    result[key] = values[idx];
  }

  return result;
}