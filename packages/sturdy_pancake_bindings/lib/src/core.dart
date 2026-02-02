// ignore_for_file: unused_element

part of '../sturdy_pancake_bindings.dart';

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

@entryPoint
JavaObject _javaObjectForId(int id) => JavaObject._allocatedInstances[id] ??= JavaObject._(id);

@entryPoint
int _idOfJavaObject(JavaObject object) => object.id;

// ---

@entryPoint
Map<K, V> _mapFromLists<K, V>(List<K> keys, List<V> values) {
  final result = <K, V>{};
  for (final (idx, key) in keys.indexed) {
    result[key] = values[idx];
  }

  return result;
}

@entryPoint
Map<String, dynamic> _jsonMapFromLists(List<dynamic> keys, List<dynamic> values) => _mapFromLists(keys.cast(), values);
