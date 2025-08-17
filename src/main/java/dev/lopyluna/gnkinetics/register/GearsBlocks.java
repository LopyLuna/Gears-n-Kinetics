package dev.lopyluna.gnkinetics.register;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.AllTags;
import com.simibubi.create.Create;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockModel;
import com.simibubi.create.foundation.block.ItemUseOverrides;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import dev.lopyluna.gnkinetics.Gears;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.ICogWheelItem;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.chainned_cog.ChainableCogwheelBlock;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.cog_crank.CogCrankBlock;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.cog_crank.CogCrankItem;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.creative_gear_motor.CreativeGearMotorBlock;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.custom_cogs.CustomCogWheelBlock;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.magnet_gears.MagnetGearBlock;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.ring_gear.RingGearBlock;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.ring_gear.RingGearItem;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.ring_gear.RingGearStructure;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.tiny_cog.TinyCogBlock;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.worm_gear.WormGearBlock;
import dev.lopyluna.gnkinetics.content.configs.server.kinetics.GStress;
import dev.lopyluna.gnkinetics.register.client.GearsPartialModels;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.client.model.generators.ModelFile;

import java.util.function.Function;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;
import static com.tterrag.registrate.providers.RegistrateRecipeProvider.has;
import static dev.lopyluna.gnkinetics.Gears.REG;

@SuppressWarnings({"removal", "unused"})
public class GearsBlocks {

    public static final BlockEntry<CreativeGearMotorBlock> CREATIVE_GEAR_MOTOR = REG.block("creative_gear_motor", CreativeGearMotorBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_PURPLE).forceSolidOn())
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .transform(pickaxeOnly())
            .blockstate(BlockStateGen.directionalBlockProviderIgnoresWaterlogged(true))
            .transform(GStress.setCapacity(16384.0))
            .onRegister(BlockStressValues.setGeneratorSpeed(256, true))
            .item()
            .properties(p -> p.rarity(Rarity.EPIC))
            .transform(customItemModel())
            .register();

    public static final BlockEntry<ChainableCogwheelBlock> CHAINABLE_COGWHEEL = REG.block("chainable_cogwheel", ChainableCogwheelBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().sound(SoundType.WOOD).mapColor(MapColor.DIRT))
            .transform(axeOrPickaxe())
            .blockstate((c, p) -> BlockStateGen.axisBlock(c, p, s -> AssetLookup.partialBaseModel(c, p)))
            .addLayer(() -> RenderType::cutoutMipped)
            .transform(GStress.setImpact(1))
            .item()
            .transform(customItemModel())
            .register();

    public static final BlockEntry<CogCrankBlock> COG_CRANK = REG.block("cog_crank", CogCrankBlock::small)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.sound(SoundType.WOOD).mapColor(MapColor.DIRT))
            .transform(axeOrPickaxe())
            .blockstate(BlockStateGen.axisBlockProvider(true))
            .transform(GStress.setCapacity(8.0))
            .onRegister(BlockStressValues.setGeneratorSpeed(32))
            .tag(AllTags.AllBlockTags.BRITTLE.tag)
            .recipe((c, p) -> ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, c.get(), 1)
                    .requires(AllBlocks.HAND_CRANK.get())
                    .requires(AllBlocks.COGWHEEL.get())
                    .unlockedBy("has_cog", has(AllBlocks.COGWHEEL.get()))
                    .save(p, Gears.loc("crafting/cog_crank")))
            .onRegister(ItemUseOverrides::addBlock)
            .item(CogCrankItem::new)
            .transform(customItemModel())
            .register();

    public static final BlockEntry<CogCrankBlock> LARGE_COG_CRANK  = REG.block("large_cog_crank", CogCrankBlock::large)
            .initialProperties(SharedProperties::wooden)
            .properties(p -> p.mapColor(MapColor.PODZOL))
            .transform(axeOrPickaxe())
            .blockstate(BlockStateGen.axisBlockProvider(true))
            .transform(GStress.setCapacity(8.0))
            .onRegister(BlockStressValues.setGeneratorSpeed(16))
            .tag(AllTags.AllBlockTags.BRITTLE.tag)
            .recipe((c, p) -> {
                ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, c.get(), 1)
                        .requires(AllBlocks.HAND_CRANK.get())
                        .requires(AllBlocks.LARGE_COGWHEEL.get())
                        .unlockedBy("has_cog", has(AllBlocks.COGWHEEL.get()))
                        .save(p, Gears.loc("crafting/large_cog_crank"));
                ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, c.get(), 1)
                        .requires(COG_CRANK.get())
                        .requires(ItemTags.PLANKS)
                        .unlockedBy("has_cog", has(AllBlocks.COGWHEEL.get()))
                        .save(p, Gears.loc("crafting/cog_crank_to_large"));
            })
            .onRegister(ItemUseOverrides::addBlock)
            .item(CogCrankItem::new)
            .transform(customItemModel())
            .register();


    public static final BlockEntry<WormGearBlock> WORM_GEAR = REG.block("worm_gear", WormGearBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.METAL).forceSolidOn())
            .transform(pickaxeOnly())
            .blockstate((c, p) -> p.directionalBlock(c.get(), s -> {
                var isFlipped = s.getValue(WormGearBlock.FACING).getAxisDirection() == Direction.AxisDirection.NEGATIVE;
                var partName = s.getValue(WormGearBlock.PART).getSerializedName();
                var flipped = isFlipped ? "_flipped" : "";
                var existing = AssetLookup.partialBaseModel(c, p, partName);
                if (!isFlipped) return existing;
                return p.models()
                        .withExistingParent("block/" + c.getName() + "_" + partName + flipped, existing.getLocation())
                        .texture("2", p.modLoc("block/" + c.getName() + flipped));
            }))
            .transform(GStress.setNoImpact())
            .item()
            .transform(customItemModel("_", "block_single"))
            .register();

    public static final BlockEntry<MagnetGearBlock> MAGNET_GEAR = REG.block("magnet_gear", MagnetGearBlock::small)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().sound(SoundType.NETHERITE_BLOCK).mapColor(MapColor.COLOR_ORANGE))
            .transform(pickaxeOnly())
            .blockstate((c, p) -> BlockStateGen.axisBlock(c, p,
                    s -> p.models().getExistingFile(p.modLoc("block/magnet_gear/small_shaftless"))))
            .addLayer(() -> RenderType::cutoutMipped)
            .transform(GStress.setNoImpact())
            .lang("Magnet Gear")
            .item(ICogWheelItem::new)
            .transform(customItemModel("magnet_gear", "small"))
            .register();

    public static final BlockEntry<MagnetGearBlock> LARGE_MAGNET_GEAR = REG.block("large_magnet_gear", MagnetGearBlock::large)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().sound(SoundType.NETHERITE_BLOCK).mapColor(MapColor.COLOR_ORANGE))
            .transform(pickaxeOnly())
            .blockstate((c, p) -> BlockStateGen.axisBlock(c, p,
                    s -> p.models().getExistingFile(p.modLoc("block/magnet_gear/large_shaftless"))))
            .addLayer(() -> RenderType::cutoutMipped)
            .transform(GStress.setNoImpact())
            .lang("Large Magnet Gear")
            .item(ICogWheelItem::new)
            .transform(customItemModel("magnet_gear", "large"))
            .register();

    public static final BlockEntry<RingGearBlock> RING_GEAR = REG.block("ring_gear", RingGearBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().isSuffocating(GearsBlocks::never).sound(SoundType.METAL).mapColor(MapColor.TERRACOTTA_YELLOW))
            .transform(pickaxeOnly())
            .blockstate((c, p) -> BlockStateGen.axisBlock(c, p, getBlockModel(true, c, p)))
            .addLayer(() -> RenderType::cutoutMipped)
            .transform(GStress.setNoImpact())
            .lang("Ring Gear")
            .item(RingGearItem::new)
            .transform(customItemModel())
            .register();

    public static final BlockEntry<RingGearStructure> RING_GEAR_STRUCT = REG.block("ring_gear_struct", RingGearStructure::new)
            .initialProperties(SharedProperties::wooden)
            .clientExtension(() -> RingGearStructure.RenderProperties::new)
            .blockstate((c, p) -> p.getVariantBuilder(c.get()).forAllStatesExcept(BlockStateGen.mapToAir(p), RingGearStructure.FACING))
            .properties(p -> p.noOcclusion().isSuffocating(GearsBlocks::never).sound(SoundType.METAL).mapColor(MapColor.TERRACOTTA_YELLOW))
            .transform(pickaxeOnly())
            .lang("Ring Gear")
            .register();

    public static final BlockEntry<TinyCogBlock> TINY_COG = REG.block("tiny_cogwheel", TinyCogBlock::shaft)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().sound(SoundType.WOOD).mapColor(MapColor.DIRT))
            .transform(axeOrPickaxe())
            .blockstate(BlockStateGen.axisBlockProvider(false))
            .addLayer(() -> RenderType::cutoutMipped)
            .transform(GStress.setNoImpact())
            .lang("Tiny Cogwheel")
            .simpleItem()
            .register();

    public static final BlockEntry<TinyCogBlock> SHAFTLESS_TINY_COG = REG.block("shaftless_tiny_cogwheel", TinyCogBlock::shaftless)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().sound(SoundType.WOOD).mapColor(MapColor.DIRT))
            .transform(axeOrPickaxe())
            .blockstate(BlockStateGen.axisBlockProvider(false))
            .addLayer(() -> RenderType::cutoutMipped)
            .transform(GStress.setNoImpact())
            .lang("Tiny Shaftless Cogwheel")
            .simpleItem()
            .register();

    public static final BlockEntry<TinyCogBlock> TINY_BRASS_COG = REG.block("tiny_brass_gear", TinyCogBlock::shaft)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().sound(SoundType.METAL).mapColor(MapColor.TERRACOTTA_YELLOW))
            .transform(pickaxeOnly())
            .blockstate(BlockStateGen.axisBlockProvider(false))
            .addLayer(() -> RenderType::cutoutMipped)
            .transform(GStress.setNoImpact())
            .lang("Tiny Brass Gear")
            .simpleItem()
            .register();

    public static final BlockEntry<TinyCogBlock> SHAFTLESS_TINY_BRASS_COG = REG.block("shaftless_tiny_brass_gear", TinyCogBlock::shaftless)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().sound(SoundType.METAL).mapColor(MapColor.TERRACOTTA_YELLOW))
            .transform(pickaxeOnly())
            .blockstate(BlockStateGen.axisBlockProvider(false))
            .addLayer(() -> RenderType::cutoutMipped)
            .transform(GStress.setNoImpact())
            .lang("Tiny Shaftless Brass Gear")
            .simpleItem()
            .register();

    public static final BlockEntry<CustomCogWheelBlock> COG_STONE = REG.block("cogstone", p ->
                    CustomCogWheelBlock.smallThick(GearsPartialModels.SHAFTLESS_COG_STONE, p))
            .transform(cogwheel("Cogstone", SoundType.STONE, MapColor.STONE))
            .item(ICogWheelItem::new)
            .build()
            .register();

    public static final BlockEntry<CustomCogWheelBlock> SHAFTLESS_COG_STONE = REG.block("shaftless_cogstone", p ->
                    CustomCogWheelBlock.smallShaftlessThick(GearsPartialModels.SHAFTLESS_COG_STONE, p))
            .transform(cogwheel("Shaftless Cogstone", SoundType.STONE, MapColor.STONE))
            .item(ICogWheelItem::new)
            .build()
            .register();

    public static final BlockEntry<CustomCogWheelBlock> ANDESITE_COG = REG.block("andesite_cogwheel", p ->
                    CustomCogWheelBlock.small(GearsPartialModels.SHAFTLESS_ANDESITE_COG, p))
            .transform(cogwheel("Andesite Cogwheel", SoundType.STONE, MapColor.STONE))
            .item(ICogWheelItem::new)
            .build()
            .register();

    public static final BlockEntry<CustomCogWheelBlock> SHAFTLESS_ANDESITE_COG = REG.block("shaftless_andesite_cogwheel", p ->
                    CustomCogWheelBlock.smallShaftless(GearsPartialModels.SHAFTLESS_ANDESITE_COG, p))
            .transform(cogwheel("Shaftless Andesite Cogwheel", SoundType.STONE, MapColor.STONE))
            .item(ICogWheelItem::new)
            .build()
            .register();



    public static final BlockEntry<CustomCogWheelBlock> INDUSTRIAL_GEAR = REG.block("industrial_gear", p ->
                    CustomCogWheelBlock.smallThick(GearsPartialModels.SHAFTLESS_INDUSTRIAL_GEAR, p))
            .transform(cogwheel("Industrial Gear", SoundType.NETHERITE_BLOCK, MapColor.COLOR_GRAY))
            .item(ICogWheelItem::new)
            .build()
            .register();

    public static final BlockEntry<CustomCogWheelBlock> LARGE_INDUSTRIAL_GEAR = REG.block("large_industrial_gear", p ->
                    CustomCogWheelBlock.largeThick(GearsPartialModels.LARGE_SHAFTLESS_INDUSTRIAL_GEAR, p))
            .transform(cogwheel("Large Industrial Gear", SoundType.NETHERITE_BLOCK, MapColor.COLOR_GRAY))
            .item(ICogWheelItem::new)
            .build()
            .register();

    public static final BlockEntry<CustomCogWheelBlock> SHAFTLESS_INDUSTRIAL_GEAR = REG.block("shaftless_industrial_gear", p ->
                    CustomCogWheelBlock.smallShaftlessThick(GearsPartialModels.SHAFTLESS_INDUSTRIAL_GEAR, p))
            .transform(cogwheel("Shaftless Industrial Gear", SoundType.NETHERITE_BLOCK, MapColor.COLOR_GRAY))
            .item(ICogWheelItem::new)
            .build()
            .register();

    public static final BlockEntry<CustomCogWheelBlock> LARGE_SHAFTLESS_INDUSTRIAL_GEAR = REG.block("shaftless_large_industrial_gear", p ->
                    CustomCogWheelBlock.largeShaftlessThick(GearsPartialModels.LARGE_SHAFTLESS_INDUSTRIAL_GEAR, p))
            .transform(cogwheel("Large Shaftless Industrial Gear", SoundType.NETHERITE_BLOCK, MapColor.COLOR_GRAY))
            .item(ICogWheelItem::new)
            .build()
            .register();

    public static final BlockEntry<CustomCogWheelBlock> BRASS_GEAR = REG.block("brass_gear", p ->
                    CustomCogWheelBlock.small(GearsPartialModels.SHAFTLESS_BRASS_GEAR, p))
            .transform(cogwheel("Brass Gear", SoundType.METAL, MapColor.TERRACOTTA_YELLOW))
            .item(ICogWheelItem::new)
            .build()
            .register();

    public static final BlockEntry<CustomCogWheelBlock> LARGE_BRASS_GEAR = REG.block("large_brass_gear", p ->
                    CustomCogWheelBlock.large(GearsPartialModels.SHAFTLESS_LARGE_BRASS_GEAR, p))
            .transform(cogwheel("Large Brass Gear", SoundType.METAL, MapColor.TERRACOTTA_YELLOW))
            .item(ICogWheelItem::new)
            .build()
            .register();

    public static final BlockEntry<CustomCogWheelBlock> HOLLOW_BRASS_GEAR = REG.block("hollow_brass_gear", p ->
                    CustomCogWheelBlock.smallHollowed(GearsPartialModels.HOLLOW_BRASS_GEAR, p))
            .transform(cogwheel("Hollowed Brass Gear", SoundType.METAL, MapColor.TERRACOTTA_YELLOW))
            .item(ICogWheelItem::new)
            .build()
            .register();

    public static final BlockEntry<CustomCogWheelBlock> LARGE_HOLLOW_BRASS_GEAR = REG.block("hollow_large_brass_gear", p ->
                    CustomCogWheelBlock.largeHollowed(GearsPartialModels.HOLLOW_LARGE_BRASS_GEAR, p))
            .transform(cogwheel("Large Hollowed Brass Gear", SoundType.METAL, MapColor.TERRACOTTA_YELLOW))
            .item(ICogWheelItem::new)
            .build()
            .register();

    public static final BlockEntry<CustomCogWheelBlock> SHAFTLESS_BRASS_GEAR = REG.block("shaftless_brass_gear", p ->
                    CustomCogWheelBlock.smallShaftless(GearsPartialModels.SHAFTLESS_BRASS_GEAR, p))
            .transform(cogwheel("Shaftless Brass Gear", SoundType.METAL, MapColor.TERRACOTTA_YELLOW))
            .item(ICogWheelItem::new)
            .build()
            .register();

    public static final BlockEntry<CustomCogWheelBlock> LARGE_SHAFTLESS_BRASS_GEAR = REG.block("shaftless_large_brass_gear", p ->
                    CustomCogWheelBlock.largeShaftless(GearsPartialModels.SHAFTLESS_LARGE_BRASS_GEAR, p))
            .transform(cogwheel("Large Shaftless Brass Gear", SoundType.METAL, MapColor.TERRACOTTA_YELLOW))
            .item(ICogWheelItem::new)
            .build()
            .register();

    public static final BlockEntry<CustomCogWheelBlock> HOLLOW_COGWHEEL = REG.block("hollow_cogwheel", p ->
                    CustomCogWheelBlock.smallHollowed(GearsPartialModels.HOLLOW_COG, p))
            .transform(cogwheel("Hollowed Cogwheel", SoundType.WOOD, MapColor.DIRT))
            .item(ICogWheelItem::new)
            .build()
            .register();

    public static final BlockEntry<CustomCogWheelBlock> LARGE_HOLLOW_COGWHEEL = REG.block("hollow_large_cogwheel", p ->
                    CustomCogWheelBlock.largeHollowed(GearsPartialModels.HOLLOW_LARGE_COG, p))
            .transform(cogwheel("Large Hollowed Cogwheel", SoundType.WOOD, MapColor.DIRT))
            .item(ICogWheelItem::new)
            .build()
            .register();

    public static final BlockEntry<CustomCogWheelBlock> SHAFTLESS_COGWHEEL = REG.block("shaftless_cogwheel", p ->
                    CustomCogWheelBlock.smallShaftless(AllPartialModels.SHAFTLESS_COGWHEEL, p))
            .transform(cogwheel("Shaftless Cogwheel", SoundType.WOOD, MapColor.DIRT, false))
            .blockstate((c, p) -> BlockStateGen.axisBlock(c, p, getBlockModelShaftless(false, c, p)))
            .item(ICogWheelItem::new)
            .model((c, p) -> getItemModelShaftless(false, c, p))
            .build()
            .register();

    public static final BlockEntry<CustomCogWheelBlock> LARGE_SHAFTLESS_COGWHEEL = REG.block("shaftless_large_cogwheel", p ->
                    CustomCogWheelBlock.largeShaftless(AllPartialModels.SHAFTLESS_LARGE_COGWHEEL, p))
            .transform(cogwheel("Large Shaftless Cogwheel", SoundType.WOOD, MapColor.DIRT, false))
            .blockstate((c, p) -> BlockStateGen.axisBlock(c, p, getBlockModelShaftless(true, c, p)))
            .item(ICogWheelItem::new)
            .model((c, p) -> getItemModelShaftless(true, c, p))
            .build()
            .register();


    public static <B extends CustomCogWheelBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> cogwheel(String lang, SoundType soundType, MapColor color) {
        return cogwheel(lang, soundType, color, true);
    }

    public static <B extends CustomCogWheelBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> cogwheel(String lang, SoundType soundType, MapColor color, boolean stateGen) {
        return builder -> {
            BlockBuilder<B, P> b = builder.initialProperties(SharedProperties::stone)
                    .properties(p -> p.noOcclusion().sound(soundType).mapColor(color))
                    .addLayer(() -> RenderType::cutoutMipped)
                    .transform(GStress.setNoImpact())
                    .transform(axeOrPickaxe())
                    .onRegister(CreateRegistrate.blockModel(() -> BracketedKineticBlockModel::new))
                    .lang(lang);
            if (stateGen) b = b.blockstate(BlockStateGen.axisBlockProvider(false));
            return b;
        };
    }


    protected static String getItemName(ItemLike pItemLike) {
        return BuiltInRegistries.ITEM.getKey(pItemLike.asItem()).getPath();
    }

    public static <T extends Block> Function<BlockState, ModelFile> getBlockModel(boolean customItem, DataGenContext<Block, T> c, RegistrateBlockstateProvider p) {
        return $ -> customItem ? AssetLookup.partialBaseModel(c, p) : AssetLookup.standardModel(c, p);
    }

    public static <T extends Block> Function<BlockState, ModelFile> getBlockModelCreate(boolean customItem, DataGenContext<Block, T> c, RegistrateBlockstateProvider p) {
        return $ -> customItem ? p.models().getExistingFile(Create.asResource("block/" + c.getName() + "/block")) : p.models().getExistingFile(Create.asResource("block/" + c.getName()));
    }

    public static <T extends Block> Function<BlockState, ModelFile> getBlockModelShaftless(boolean isLarge, DataGenContext<Block, T> c, RegistrateBlockstateProvider p) {
        return $ -> p.models().getExistingFile(Create.asResource("block/" + (isLarge ? "large_cogwheel" : "cogwheel") + "_shaftless"));
    }
    public static <I extends BlockItem> void getItemModelShaftless(boolean isLarge, DataGenContext<Item, I> c, RegistrateItemModelProvider p) {
        p.withExistingParent(c.getName(), Create.asResource("block/" + (isLarge ? "large_cogwheel" : "cogwheel") + "_shaftless"));
    }

    private static boolean never(BlockState state, BlockGetter blockGetter, BlockPos pos) {
        return false;
    }

    public static void register() {}
}
