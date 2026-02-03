import 'package:collection/collection.dart';
import 'package:syrup/syrup.dart';

final class Identifier {
  final String namespace;
  final String path;

  const Identifier(this.namespace, this.path);
  const Identifier.minecraft({required String path}) : this('minecraft', path);

  factory Identifier.parse(String stringId) {
    final separatorIdx = stringId.indexOf(':');
    return separatorIdx != -1
        ? Identifier(stringId.substring(0, separatorIdx), stringId.substring(separatorIdx + 1))
        : Identifier.minecraft(path: stringId);
  }

  @override
  int get hashCode => Object.hash(namespace, path);

  @override
  bool operator ==(Object other) => other is Identifier && other.namespace == namespace && other.path == path;

  @override
  String toString() => '$namespace:$path';
}

sealed class Ingredient {
  Object? toJson();
  const Ingredient();

  const factory Ingredient.item(Identifier itemId) = ItemIngredient._;
  const factory Ingredient.tag(Identifier tagId) = TagIngredient._;
  const factory Ingredient.list(List<Identifier> itemIds) = ListIngredient._;

  factory Ingredient.fromJson(Object? json) => switch (json) {
    String id when !id.startsWith('#') => .item(Identifier.parse(id)),
    String tagId => .tag(Identifier.parse(tagId.substring(1))),
    List<dynamic> ids when ids.every((element) => element is String) => .list([
      for (String id in ids) Identifier.parse(id),
    ]),
    _ => throw 'unsupported identifier format',
  };
}

final class ItemIngredient extends Ingredient {
  final Identifier itemId;
  const ItemIngredient._(this.itemId);

  @override
  Object? toJson() => '$itemId';

  @override
  int get hashCode => itemId.hashCode;

  @override
  bool operator ==(Object other) => other is ItemIngredient && other.itemId == itemId;
}

final class TagIngredient extends Ingredient {
  final Identifier tagId;
  const TagIngredient._(this.tagId);

  @override
  Object? toJson() => '#$tagId';

  @override
  int get hashCode => tagId.hashCode;

  @override
  bool operator ==(Object other) => other is TagIngredient && other.tagId == tagId;
}

final class ListIngredient extends Ingredient {
  final List<Identifier> itemIds;
  const ListIngredient._(this.itemIds);

  @override
  Object? toJson() => itemIds.map((id) => id.toString()).toList();

  @override
  int get hashCode => const ListEquality<Identifier>().hash(itemIds);

  @override
  bool operator ==(Object other) =>
      other is ListIngredient && const ListEquality<Identifier>().equals(other.itemIds, itemIds);
}

final class IngredientNode extends MutatorNode<Ingredient> {
  const IngredientNode();

  @override
  Ingredient decode(Object? json) => Ingredient.fromJson(json);

  @override
  Object? encode(Ingredient value) => value.toJson();
}
