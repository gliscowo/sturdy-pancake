import 'package:syrup/syrup.dart';

const TypeTreeNode blastingType = ObjectNode(fields: {'ingredient': IngredientNode(), 'result': RecipeResultNode()});

const TypeTreeNode campfireCookingType = ObjectNode(
  fields: {'ingredient': IngredientNode(), 'result': RecipeResultNode()},
);

const TypeTreeNode craftingShapedType = ObjectNode(
  fields: {
    'key': MapNode(valueType: IngredientNode()),
    'result': RecipeResultNode(),
  },
);

const TypeTreeNode craftingShapelessType = ObjectNode(
  fields: {
    'ingredients': ListNode(elementType: IngredientNode()),
    'result': RecipeResultNode(),
  },
);

const TypeTreeNode craftingTransmuteType = ObjectNode(
  fields: {'input': IngredientNode(), 'material': IngredientNode(), 'result': RecipeResultNode()},
);

const TypeTreeNode smeltingType = ObjectNode(fields: {'ingredient': IngredientNode(), 'result': RecipeResultNode()});

const TypeTreeNode smithingTransformType = ObjectNode(
  fields: {
    'template': IngredientNode(),
    'base': IngredientNode(),
    'addition': IngredientNode(),
    'result': RecipeResultNode(),
  },
);

const TypeTreeNode smithingTrimType = ObjectNode(
  fields: {'template': IngredientNode(), 'base': IngredientNode(), 'addition': IngredientNode()},
);

const TypeTreeNode smokingType = ObjectNode(fields: {'ingredient': IngredientNode(), 'result': RecipeResultNode()});

const TypeTreeNode stonecuttingType = ObjectNode(
  fields: {'ingredient': IngredientNode(), 'result': RecipeResultNode()},
);

// ---

final recipeIdToType = <Identifier, TypeTreeNode>{
  .minecraft(path: 'blasting'): blastingType,
  .minecraft(path: 'campfire_cooking'): campfireCookingType,
  .minecraft(path: 'crafting_shaped'): craftingShapedType,
  .minecraft(path: 'crafting_shapeless'): craftingShapelessType,
  .minecraft(path: 'crafting_transmute'): craftingTransmuteType,
  .minecraft(path: 'smelting'): smeltingType,
  .minecraft(path: 'smithing_transform'): smithingTransformType,
  .minecraft(path: 'smithing_trim'): smithingTrimType,
  .minecraft(path: 'smoking'): smokingType,
  .minecraft(path: 'stonecutting'): stonecuttingType,
};

const TypeTreeNode recipeType = DispatchNode(dispatcher: _recipeDispatcher);
TypeTreeNode? _recipeDispatcher(Object? json) {
  if (json case {'type': String recipeTypeId}) {
    return recipeIdToType[Identifier.parse(recipeTypeId)];
  }

  return null;
}
