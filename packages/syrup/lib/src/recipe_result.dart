import 'package:collection/collection.dart';
import 'package:syrup/syrup.dart';

class RecipeResult {
  final Identifier itemId;
  final int count;
  final List<Object?> components;

  const RecipeResult({required this.itemId, this.count = 1, this.components = const []});

  factory RecipeResult.fromJson(Object? json) {
    if (json is! Map<String, dynamic>) {
      throw 'invalid item stack';
    }

    final id = Identifier.parse(json['id'] as String);
    final count = (json['count'] as int?) ?? 0;
    final components = (json['components'] as List<dynamic>?) ?? [];

    return RecipeResult(itemId: id, count: count, components: components.cast());
  }

  Object? toJson() => {
    'id': itemId.toString(),
    if (count != 1) 'count': count,
    if (components.isNotEmpty) 'components': components,
  };

  @override
  int get hashCode => Object.hash(itemId, count, const ListEquality<Object?>().hash(components));

  @override
  bool operator ==(Object other) =>
      other is RecipeResult &&
      other.itemId == itemId &&
      other.count == count &&
      const ListEquality<Object?>().equals(other.components, components);
}

class RecipeResultNode extends MutatorNode<RecipeResult> {
  const RecipeResultNode();

  @override
  RecipeResult decode(Object? json) => RecipeResult.fromJson(json);

  @override
  Object? encode(RecipeResult value) => value.toJson();
}
