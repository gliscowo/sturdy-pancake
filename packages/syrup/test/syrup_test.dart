import 'package:syrup/syrup.dart';
import 'package:test/test.dart';

void main() {
  test('ingredient parsing', () {
    expect(
      Ingredient.fromJson('an_item'),
      isA<ItemIngredient>().having(
        (ingredient) => ingredient.itemId,
        'itemId',
        const Identifier.minecraft(path: 'an_item'),
      ),
    );

    expect(
      Ingredient.fromJson('#non_vanilla:tag'),
      isA<TagIngredient>().having((ingredient) => ingredient.tagId, 'tagId', const Identifier('non_vanilla', 'tag')),
    );

    expect(
      Ingredient.fromJson(['an_item', 'another:item']),
      isA<ListIngredient>().having((ingredient) => ingredient.itemIds, 'itemId', const [
        Identifier.minecraft(path: 'an_item'),
        Identifier('another', 'item'),
      ]),
    );
  });

  test('shaped recipe type tree', () {
    final recipeJson = {
      "type": "minecraft:crafting_shaped",
      "category": "misc",
      "key": {"A": "minecraft:milk_bucket", "B": "minecraft:sugar", "C": "minecraft:wheat", "E": "#minecraft:eggs"},
      "pattern": ["AAA", "BEB", "CCC"],
      "result": {"count": 1, "id": "minecraft:cake"},
    };

    final alteredRecipeJson = {
      "type": "minecraft:crafting_shaped",
      "category": "misc",
      "key": {
        "A": "minecraft:milk_bucket",
        "B": "#minecraft:logs",
        "C": "minecraft:wheat",
        "E": "minecraft:lava_bucket",
      },
      "pattern": ["AAA", "BEB", "CCC"],
      "result": {"id": "minecraft:pumpkin_pie"},
    };

    expect(recipeJson, isNot(alteredRecipeJson));

    final replaceSugarwithLogs = forType<Ingredient>(
      remap(const ItemIngredient(.minecraft(path: 'sugar')), const TagIngredient(.minecraft(path: 'logs'))),
    );

    final setEToLava = forType<Ingredient>(
      atKey('E', overwrite(const ItemIngredient(.minecraft(path: 'lava_bucket')))),
    );

    final replaceCakeWithPumpkinPie = forType<RecipeResult>(
      remap(
        const RecipeResult(itemId: .minecraft(path: 'cake')),
        const RecipeResult(itemId: .minecraft(path: 'pumpkin_pie')),
      ),
    );

    final rewriteRule = sequential([replaceSugarwithLogs, replaceCakeWithPumpkinPie, setEToLava]);
    rewrite(recipeJson, recipeType, rewriteRule);

    expect(recipeJson, alteredRecipeJson);
  });
}
