part of '../sturdy_pancake_bindings.dart';

@entryPoint
bool includeRecipe(String recipeId) {
  return recipeId != 'minecraft:stone_button';
}

@entryPoint
bool rewriteRecipe(String recipeId, Object? recipeJson) {
  final rule = forType<Ingredient>(
    remap(const TagIngredient(.minecraft(path: 'planks')), const ItemIngredient(.minecraft(path: 'cooked_beef'))),
  );

  return rewrite(recipeJson, recipeType, rule);
}
