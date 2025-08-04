package dev.lopyluna.gnkinetics.content.datagen.recipes;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.concurrent.CompletableFuture;

import static dev.lopyluna.gnkinetics.Gears.MOD_ID;

@SuppressWarnings("unused")
public final class ItemApplicationRecipeGen extends com.simibubi.create.api.data.recipe.ItemApplicationRecipeGen {


    public ItemApplicationRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, MOD_ID);
    }
    public static Ingredient items(ItemLike... items) {
        return Ingredient.of(items);
    }
}
