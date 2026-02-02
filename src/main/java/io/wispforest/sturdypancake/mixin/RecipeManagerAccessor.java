package io.wispforest.sturdypancake.mixin;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RecipeManager.class)
public interface RecipeManagerAccessor {
    @Accessor("RECIPE_LISTER")
    static FileToIdConverter RECIPE_LISTER() { throw new UnsupportedOperationException(); }
}
