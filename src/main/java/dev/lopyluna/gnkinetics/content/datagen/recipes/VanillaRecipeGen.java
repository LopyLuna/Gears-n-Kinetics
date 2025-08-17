package dev.lopyluna.gnkinetics.content.datagen.recipes;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.api.data.recipe.BaseRecipeProvider;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import dev.lopyluna.gnkinetics.Gears;
import dev.lopyluna.gnkinetics.register.GearsBlocks;
import dev.lopyluna.gnkinetics.register.GearsTags;
import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.conditions.NotCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.UnaryOperator;

@SuppressWarnings({"unused", "SameParameterValue"})
public class VanillaRecipeGen extends BaseRecipeProvider {
    final List<GeneratedRecipe> all = new ArrayList<>();

    GeneratedRecipe MAGNET_GEAR = create(GearsBlocks.MAGNET_GEAR).unlockedBy(AllItems.TRANSMITTER::get)
            .viaShapeless(b -> b
                    .requires(Ingredient.of(GearsBlocks.INDUSTRIAL_GEAR.get(), GearsBlocks.SHAFTLESS_INDUSTRIAL_GEAR.get()))
                    .requires(AllItems.TRANSMITTER));

    GeneratedRecipe LARGE_MAGNET_GEAR = create(GearsBlocks.LARGE_MAGNET_GEAR).unlockedBy(AllItems.TRANSMITTER::get)
            .viaShapeless(b -> b
                    .requires(Ingredient.of(GearsBlocks.LARGE_INDUSTRIAL_GEAR.get(), GearsBlocks.LARGE_SHAFTLESS_INDUSTRIAL_GEAR.get()))
                    .requires(AllItems.TRANSMITTER)
                    .requires(AllItems.TRANSMITTER));

    GeneratedRecipe WORM_GEAR = create(GearsBlocks.WORM_GEAR).returns(8).unlockedBy(AllItems.IRON_SHEET::get)
            .viaShaped(b -> b
                    .define('i', GearsTags.commonItemTag("plates/iron"))
                    .define('a', AllItems.ANDESITE_ALLOY)
                    .pattern("a")
                    .pattern("i")
                    .pattern("a"));

    GeneratedRecipe RING_GEAR = create(GearsBlocks.RING_GEAR).unlockedBy(AllItems.BRASS_INGOT::get)
            .viaShaped(b -> b
                    .define('b', GearsTags.commonItemTag("ingots/brass"))
                    .define('g', GearsBlocks.LARGE_HOLLOW_BRASS_GEAR)
                    .pattern("bbb")
                    .pattern("bgb")
                    .pattern("bbb"));


    GeneratedRecipe CHAINABLE_COGWHEEL = create(GearsBlocks.CHAINABLE_COGWHEEL).unlockedBy(AllBlocks.ANDESITE_CASING::get)
            .viaShapeless(b -> b
                    .requires(AllBlocks.LARGE_COGWHEEL.get())
                    .requires(AllBlocks.ANDESITE_CASING)
                    .requires(GearsTags.commonItemTag("nuggets/iron"))
                    .requires(GearsTags.commonItemTag("nuggets/iron")));

    GeneratedRecipe CHAINABLE_COGWHEEL_ZINC = create(GearsBlocks.CHAINABLE_COGWHEEL).withSuffix("_from_zinc").unlockedBy(AllBlocks.ANDESITE_CASING::get)
            .viaShapeless(b -> b
                    .requires(AllBlocks.LARGE_COGWHEEL.get())
                    .requires(AllBlocks.ANDESITE_CASING)
                    .requires(GearsTags.commonItemTag("nuggets/zinc"))
                    .requires(GearsTags.commonItemTag("nuggets/zinc")));

    GeneratedRecipe TINY_COGWHEEL = create(GearsBlocks.TINY_COG).unlockedBy(AllItems.ANDESITE_ALLOY::get)
            .viaShapeless(b -> b
                    .requires(AllBlocks.SHAFT.get())
                    .requires(ItemTags.WOODEN_SLABS));

    GeneratedRecipe TINY_GEAR = create(GearsBlocks.TINY_BRASS_COG).unlockedBy(AllItems.BRASS_INGOT::get)
            .viaShapeless(b -> b
                    .requires(AllBlocks.SHAFT.get())
                    .requires(GearsTags.commonItemTag("nuggets/brass"))
                    .requires(GearsTags.commonItemTag("nuggets/brass"))
                    .requires(GearsTags.commonItemTag("nuggets/brass")));

    GeneratedRecipe GEAR = create(GearsBlocks.BRASS_GEAR).unlockedBy(AllItems.BRASS_INGOT::get)
			.viaShapeless(b -> b
                    .requires(AllBlocks.SHAFT.get())
                    .requires(GearsTags.commonItemTag("ingots/brass")));

    GeneratedRecipe LARGE_GEAR = create(GearsBlocks.LARGE_BRASS_GEAR).unlockedBy(AllItems.BRASS_INGOT::get)
			.viaShapeless(b -> b
                    .requires(AllBlocks.SHAFT.get())
                    .requires(GearsTags.commonItemTag("ingots/brass"))
                    .requires(GearsTags.commonItemTag("ingots/brass")));

    GeneratedRecipe INDUSTRIAL_GEAR = create(GearsBlocks.INDUSTRIAL_GEAR).unlockedBy(AllBlocks.INDUSTRIAL_IRON_BLOCK::get)
            .viaShapeless(b -> b
                    .requires(AllBlocks.SHAFT.get())
                    .requires(AllBlocks.INDUSTRIAL_IRON_BLOCK.get()));

    GeneratedRecipe INDUSTRIAL_LARGE_GEAR = create(GearsBlocks.LARGE_INDUSTRIAL_GEAR).unlockedBy(AllBlocks.INDUSTRIAL_IRON_BLOCK::get)
            .viaShapeless(b -> b
                    .requires(AllBlocks.SHAFT.get())
                    .requires(AllBlocks.INDUSTRIAL_IRON_BLOCK.get())
                    .requires(AllBlocks.INDUSTRIAL_IRON_BLOCK.get()));

    GeneratedRecipe COGSTONE = create(GearsBlocks.COG_STONE).unlockedBy(AllBlocks.SHAFT::get)
            .viaShapeless(b -> b
                    .requires(AllBlocks.SHAFT.get())
                    .requires(GearsTags.commonItemTag("stones")));

    GeneratedRecipe ANDESITE_COG = create(GearsBlocks.ANDESITE_COG).unlockedBy(AllBlocks.SHAFT::get)
            .viaShapeless(b -> b
                    .requires(AllBlocks.SHAFT.get())
                    .requires(AllItems.ANDESITE_ALLOY.get()));

    GeneratedRecipe COGSTONE_CYCLE = conversionCycle(ImmutableList.of(GearsBlocks.COG_STONE, GearsBlocks.SHAFTLESS_COG_STONE));
    GeneratedRecipe ANDESITE_COG_CYCLE = conversionCycle(ImmutableList.of(GearsBlocks.ANDESITE_COG, GearsBlocks.SHAFTLESS_ANDESITE_COG));

    GeneratedRecipe INDUSTRIAL_GEAR_CYCLE = conversionCycle(ImmutableList.of(GearsBlocks.INDUSTRIAL_GEAR, GearsBlocks.SHAFTLESS_INDUSTRIAL_GEAR));
    GeneratedRecipe INDUSTRIAL_LARGE_GEAR_CYCLE = conversionCycle(ImmutableList.of(GearsBlocks.LARGE_INDUSTRIAL_GEAR, GearsBlocks.LARGE_SHAFTLESS_INDUSTRIAL_GEAR));

    GeneratedRecipe TINY_COGWHEEL_CYCLE = conversionCycle(ImmutableList.of(GearsBlocks.TINY_COG, GearsBlocks.SHAFTLESS_TINY_COG));
    GeneratedRecipe COGWHEEL_CYCLE = conversionCycle(ImmutableList.of(AllBlocks.COGWHEEL, GearsBlocks.SHAFTLESS_COGWHEEL, GearsBlocks.HOLLOW_COGWHEEL));
    GeneratedRecipe LARGE_COGWHEEL_CYCLE = conversionCycle(ImmutableList.of(AllBlocks.LARGE_COGWHEEL, GearsBlocks.LARGE_SHAFTLESS_COGWHEEL, GearsBlocks.LARGE_HOLLOW_COGWHEEL));

    GeneratedRecipe TINY_BRASS_GEAR_CYCLE = conversionCycle(ImmutableList.of(GearsBlocks.TINY_BRASS_COG, GearsBlocks.SHAFTLESS_TINY_BRASS_COG));
    GeneratedRecipe BRASS_GEAR_CYCLE = conversionCycle(ImmutableList.of(GearsBlocks.BRASS_GEAR, GearsBlocks.SHAFTLESS_BRASS_GEAR, GearsBlocks.HOLLOW_BRASS_GEAR));
    GeneratedRecipe LARGE_BRASS_GEAR_CYCLE = conversionCycle(ImmutableList.of(GearsBlocks.LARGE_BRASS_GEAR, GearsBlocks.LARGE_SHAFTLESS_BRASS_GEAR, GearsBlocks.LARGE_HOLLOW_BRASS_GEAR));
    
    static class Marker {}
    
    String currentFolder = "";

    Marker enterFolder(String folder) {
        currentFolder = folder;
        return new Marker();
    }

    GeneratedRecipeBuilder create(Supplier<ItemLike> result) {
        return new GeneratedRecipeBuilder(currentFolder, result);
    }

    GeneratedRecipeBuilder create(ResourceLocation result) {
        return new GeneratedRecipeBuilder(currentFolder, result);
    }

    GeneratedRecipeBuilder create(ItemProviderEntry<? extends ItemLike, ? extends ItemLike> result) {
        return create(result::get);
    }

    GeneratedRecipe createSpecial(Function<CraftingBookCategory, Recipe<?>> builder, String recipeType,
                                 String path) {
        return register(consumer -> SpecialRecipeBuilder.special(builder).save(consumer, Gears.loc(recipeType + "/" + currentFolder + "/" + path).toString()));
    }


    GeneratedRecipe conversionCycle(List<ItemProviderEntry<? extends ItemLike, ? extends ItemLike>> cycle) {
        GeneratedRecipe result = null;
        for (int i = 0; i < cycle.size(); i++) {
            ItemProviderEntry<? extends ItemLike, ? extends ItemLike> currentEntry = cycle.get(i);
            ItemProviderEntry<? extends ItemLike, ? extends ItemLike> nextEntry = cycle.get((i + 1) % cycle.size());
            result = create(nextEntry).withSuffix("_from_conversion")
                    .unlockedBy(currentEntry::get)
                    .viaShapeless(b -> b.requires(currentEntry.get()));
        }
        return result;
    }

    GeneratedRecipe clearData(ItemProviderEntry<? extends ItemLike, ? extends ItemLike> item) {
        return create(item).withSuffix("_clear")
                .unlockedBy(item::get)
                .viaShapeless(b -> b.requires(item.get()));
    }

    @Override
    public void buildRecipes(RecipeOutput output) {
        all.forEach(c -> c.register(output));
        Gears.LOGGER.info("{} registered {} recipe{}", getName(), all.size(), all.size() == 1 ? "" : "s");
    }

    protected GeneratedRecipe register(GeneratedRecipe recipe) {
        all.add(recipe);
        return recipe;
    }

    class GeneratedRecipeBuilder {

        private final String path;
        private String suffix;
        private Supplier<? extends ItemLike> result;
        private ResourceLocation compatDatagenOutput;
        List<ICondition> recipeConditions;

        private Supplier<ItemPredicate> unlockedBy;
        private int amount;

        private GeneratedRecipeBuilder(String path) {
            this.path = path;
            this.recipeConditions = new ArrayList<>();
            this.suffix = "";
            this.amount = 1;
        }

        public GeneratedRecipeBuilder(String path, Supplier<? extends ItemLike> result) {
            this(path);
            this.result = result;
        }

        public GeneratedRecipeBuilder(String path, ResourceLocation result) {
            this(path);
            this.compatDatagenOutput = result;
        }

        GeneratedRecipeBuilder returns(int amount) {
            this.amount = amount;
            return this;
        }

        GeneratedRecipeBuilder unlockedBy(Supplier<? extends ItemLike> item) {
            this.unlockedBy = () -> ItemPredicate.Builder.item().of(item.get()).build();
            return this;
        }

        GeneratedRecipeBuilder unlockedByTag(Supplier<TagKey<Item>> tag) {
            this.unlockedBy = () -> ItemPredicate.Builder.item().of(tag.get()).build();
            return this;
        }

        GeneratedRecipeBuilder whenModLoaded(String modid) {
            return withCondition(new ModLoadedCondition(modid));
        }

        GeneratedRecipeBuilder whenModMissing(String modid) {
            return withCondition(new NotCondition(new ModLoadedCondition(modid)));
        }

        GeneratedRecipeBuilder withCondition(ICondition condition) {
            recipeConditions.add(condition);
            return this;
        }

        GeneratedRecipeBuilder withSuffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        GeneratedRecipe viaShaped(UnaryOperator<ShapedRecipeBuilder> builder) {
            return register(consumer -> {
                var b = builder.apply(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result.get(), amount));
                if (unlockedBy != null) b.unlockedBy("has_item", inventoryTrigger(unlockedBy.get()));
                b.save(consumer, createLocation());
            });
        }

        GeneratedRecipe viaShapeless(UnaryOperator<ShapelessRecipeBuilder> builder) {
            return register(recipeOutput -> {
                var b = builder.apply(ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, result.get(), amount));
                if (unlockedBy != null) b.unlockedBy("has_item", inventoryTrigger(unlockedBy.get()));
                b.save(recipeOutput, createLocation());
            });
        }

        private ResourceLocation createSimpleLocation(String recipeType) {
            return Gears.loc(recipeType + "/" + getRegistryName().getPath() + suffix);
        }

        private ResourceLocation createLocation() {
            if (path == null || path.isEmpty() || path.equals("null")) return Gears.loc("crafting" + "/" + getRegistryName().getPath() + suffix);
            return Gears.loc("crafting" + "/" + path + "/" + getRegistryName().getPath() + suffix);
        }

        private ResourceLocation getRegistryName() {
            return compatDatagenOutput == null ? RegisteredObjectsHelper.getKeyOrThrow(result.get().asItem()) : compatDatagenOutput;
        }
    }
    
    public VanillaRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, Gears.MOD_ID);
    }
}
