part of '../sturdy_pancake_bindings.dart';

typedef RecipeFilter = bool Function(String recipeId);
final _recipeExclusions = <RecipeFilter>[];

RecipeFilter allRecipes() =>
    (_) => true;

RecipeFilter matchIds(Set<Identifier> excludedIds) {
  final excludedIdStrings = excludedIds.map((e) => e.toString()).toSet();
  return (recipeId) => excludedIdStrings.contains(recipeId);
}

RecipeFilter matchRegex(String regex) {
  final compiledRegex = RegExp(regex);
  return (recipeId) => compiledRegex.hasMatch(recipeId);
}

void addRecipeExclusion(RecipeFilter filter) {
  _recipeExclusions.add(filter);
}

@entryPoint
bool includeRecipe(String recipeId) {
  for (final filter in _recipeExclusions) {
    if (filter(recipeId)) {
      return false;
    }
  }

  return true;
}

// ---

final _rewriteRules = <({RecipeFilter filter, RewriteRule rule})>[];
void addRecipeRewriteRule(RecipeFilter filter, RewriteRule rule) {
  _rewriteRules.add((filter: filter, rule: rule));
}

@entryPoint
bool rewriteRecipe(String recipeId, Object? recipeJson) {
  final applicableRules = _rewriteRules.where((e) => e.filter(recipeId)).map((e) => e.rule).toList();
  if (applicableRules.isEmpty) {
    return false;
  }

  return rewrite(recipeJson, recipeType, sequential(applicableRules));
}
