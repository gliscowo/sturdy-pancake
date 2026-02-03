package io.wispforest.sturdypancake.mixin;

import com.google.common.collect.Iterators;
import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.wispforest.sturdypancake.BindingUtilsKt;
import io.wispforest.sturdypancake.SturdyPancake;
import io.wispforest.sturdypancake.syrup.JsonMarshallingKt;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.lang.foreign.MemorySegment;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Mixin(SimpleJsonResourceReloadListener.class)
public class SimpleJsonResourceReloadListenerMixin {

    @ModifyReceiver(
        method = "scanDirectory(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/FileToIdConverter;Lcom/mojang/serialization/DynamicOps;Lcom/mojang/serialization/Codec;Ljava/util/Map;)V",
        at = @At(value = "INVOKE", target = "Ljava/util/Set;iterator()Ljava/util/Iterator;")
    )
    private static Set<Map.Entry<Identifier, Resource>> filterRecipes(Set<Map.Entry<Identifier, Resource>> instance, ResourceManager resourceManager, FileToIdConverter fileToIdConverter, @Share("excluded_recipes") LocalRef<Set<Identifier>> excludedRecipesShare) {
        if (fileToIdConverter != RecipeManagerAccessor.RECIPE_LISTER()) {
            return instance;
        }

        var excludedRecipes = new HashSet<Identifier>();
        SturdyPancake.INSTANCE.getDartEnvironment().runInMainIsolate(it -> {
            for (var entry : instance) {
                var result = it.invoke(
                    "includeRecipe",
                    new MemorySegment[]{BindingUtilsKt.toDartHandle(RecipeManagerAccessor.RECIPE_LISTER().fileToId(entry.getKey()).toString())},
                    it.getBindingsLibrary()
                );

                if (!BindingUtilsKt.getDartBool(result)) {
                    excludedRecipes.add(entry.getKey());
                }
            }

            return null;
        });

        excludedRecipesShare.set(excludedRecipes);

        return instance;
    }

    @ModifyExpressionValue(
        method = "scanDirectory(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/FileToIdConverter;Lcom/mojang/serialization/DynamicOps;Lcom/mojang/serialization/Codec;Ljava/util/Map;)V",
        at = @At(value = "INVOKE", target = "Ljava/util/Set;iterator()Ljava/util/Iterator;")
    )
    private static Iterator<Map.Entry<Identifier, Resource>> replaceRecipeIterator(Iterator<Map.Entry<Identifier, Resource>> original, @Share("excluded_recipes") LocalRef<Set<Identifier>> excludedRecipesShare) {
        var excludedRecipes = excludedRecipesShare.get();
        if (excludedRecipes == null) {
            return original;
        }

        return Iterators.filter(
            original,
            input -> !excludedRecipes.contains(input.getKey())
        );
    }

    @ModifyExpressionValue(
        method = "scanDirectory(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/FileToIdConverter;Lcom/mojang/serialization/DynamicOps;Lcom/mojang/serialization/Codec;Ljava/util/Map;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/StrictJsonParser;parse(Ljava/io/Reader;)Lcom/google/gson/JsonElement;")
    )
    private static JsonElement transformRecipes(JsonElement original, ResourceManager resourceManager, FileToIdConverter fileToIdConverter, @Local(ordinal = 1) Identifier recipeId) {
        if (fileToIdConverter != RecipeManagerAccessor.RECIPE_LISTER()) {
            return original;
        }

        return SturdyPancake.INSTANCE.getDartEnvironment().runInMainIsolate(it -> {
            var dartJson = JsonMarshallingKt.jsonToDartObject(original);

            var anyModifications = it.invoke(
                "rewriteRecipe",
                new MemorySegment[]{BindingUtilsKt.toDartHandle(recipeId.toString()), dartJson},
                it.getBindingsLibrary()
            );

            return BindingUtilsKt.getDartBool(anyModifications)
                ? JsonMarshallingKt.dartObjectToJson(dartJson)
                : original;
        });
    }
}
