import 'dart:collection';

import 'package:syrup/syrup.dart';

sealed class TypeTreeNode {
  const TypeTreeNode();
}

final class ListNode extends TypeTreeNode {
  final TypeTreeNode elementType;
  const ListNode({required this.elementType});
}

final class MapNode extends TypeTreeNode {
  final TypeTreeNode valueType;
  const MapNode({required this.valueType});
}

final class ObjectNode extends TypeTreeNode {
  final Map<String, TypeTreeNode> fields;
  const ObjectNode({required this.fields});
}

typedef TypeDispatcher = TypeTreeNode? Function(Object? json);

final class DispatchNode extends TypeTreeNode {
  final TypeDispatcher dispatcher;
  const DispatchNode({required this.dispatcher});
}

abstract class MutatorNode<T> extends TypeTreeNode {
  final TypeTreeNode? underlying;
  const MutatorNode({this.underlying});

  T decode(Object? json);
  Object? encode(T value);
}

// ---

sealed class LocationSegment {}

final class IndexLocationSegment extends LocationSegment {
  final int index;
  IndexLocationSegment._(this.index);
}

final class KeyLocationSegment extends LocationSegment {
  final String key;
  KeyLocationSegment._(this.key);
}

extension type const Location._(List<LocationSegment> segments) {
  const Location() : this._(const []);

  Location _and(LocationSegment segment) => Location._(UnmodifiableListView([...segments, segment]));
  Location andIndex(int index) => _and(IndexLocationSegment._(index));
  Location andKey(String key) => _and(KeyLocationSegment._(key));
}

// ---

const _noUpdateSentinel = Object();

void rewrite(Object? json, TypeTreeNode type, RewriteRule rewriteRule) =>
    _rewrite(json, const Location(), type, rewriteRule);

Object? _rewrite(Object? json, Location location, TypeTreeNode type, RewriteRule rewriteRule) {
  switch (type) {
    case ListNode(:final elementType) when json is List<dynamic>:
      Map<int, Object?>? replacements;

      for (final (idx, element) in json.indexed) {
        final result = _rewrite(element, location.andIndex(idx), elementType, rewriteRule);
        if (!identical(result, _noUpdateSentinel)) {
          (replacements ??= {})[idx] = result;
        }
      }

      if (replacements != null) {
        for (final MapEntry(key: idx, value: replacement) in replacements.entries) {
          json[idx] = replacement;
        }
      }

      return _noUpdateSentinel;

    case MapNode(:final valueType) when json is Map<String, dynamic>:
      Map<String, Object?>? replacements;

      for (final MapEntry(:key, :value) in json.entries) {
        final result = _rewrite(value, location.andKey(key), valueType, rewriteRule);
        if (!identical(result, _noUpdateSentinel)) {
          (replacements ??= {})[key] = result;
        }
      }

      if (replacements != null) {
        for (final MapEntry(key: idx, value: replacement) in replacements.entries) {
          json[idx] = replacement;
        }
      }

      return _noUpdateSentinel;

    case ObjectNode(:final fields):
      final jsonObject = json as Map<String, dynamic>;
      for (final MapEntry(key: fieldName, value: fieldType) in fields.entries) {
        final result = _rewrite(jsonObject[fieldName], location.andKey(fieldName), fieldType, rewriteRule);
        if (!identical(result, _noUpdateSentinel)) {
          jsonObject[fieldName] = result;
        }
      }

      return _noUpdateSentinel;

    case DispatchNode(:final dispatcher):
      final resolvedType = dispatcher(json);
      if (resolvedType == null) {
        return _noUpdateSentinel;
      }

      return _rewrite(json, location, resolvedType, rewriteRule);

    case MutatorNode mutator:
      Object? result = _noUpdateSentinel;
      rewriteRule(json, location, mutator, (json) => result = json);

      if (identical(result, _noUpdateSentinel) && mutator.underlying != null) {
        return _rewrite(json, location, mutator.underlying!, rewriteRule);
      } else {
        return result;
      }
    case _:
      return _noUpdateSentinel;
  }
}
