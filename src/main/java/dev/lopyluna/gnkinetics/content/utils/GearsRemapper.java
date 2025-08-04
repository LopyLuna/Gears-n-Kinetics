package dev.lopyluna.gnkinetics.content.utils;

import com.simibubi.create.foundation.utility.BlockHelper;
import dev.lopyluna.gnkinetics.register.GearsBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GearsRemapper {
    private static final List<String> TARGET_MOD_IDS = new ArrayList<>();
    private static final Map<Item, Item> ITEM_REMAP = new HashMap<>();
    private static final Map<Block, Block> BLOCK_REMAP = new HashMap<>();

    public static Item remapItem(Item item) {
        var newItem = ITEM_REMAP.get(item);
        return newItem != null ? newItem : item;
    }

    public static ItemStack remapItemStack(ItemStack stack) {
        var item = ITEM_REMAP.get(stack.getItem());
        return item != null ? stack.transmuteCopy(item) : stack;
    }

    public static Block remapBlock(Block block) {
        var newBlock = BLOCK_REMAP.get(block);
        return newBlock != null ? newBlock : block;
    }

    public static BlockState remapBlockState(BlockState state) {
        var block = BLOCK_REMAP.get(state.getBlock());
        return block != null ? BlockHelper.copyProperties(state, block.defaultBlockState()) : state;
    }

    public static void register() {
        TARGET_MOD_IDS.add("dndecor");
        TARGET_MOD_IDS.add("dndesires");
        buildBlockRemap();
        buildItemRemap();
    }

    public static void buildBlockRemap() {
        for (var entry : BuiltInRegistries.BLOCK.entrySet()) {
            var block = entry.getValue();
            var blockID = BuiltInRegistries.BLOCK.getKey(block);
            if (!TARGET_MOD_IDS.contains(blockID.getNamespace())) continue;
            var remapped = remapBlocks(blockID);
            if (remapped == null) continue;
            BLOCK_REMAP.put(block, remapped);
        }
    }

    public static void buildItemRemap() {
        for (var entry : BuiltInRegistries.ITEM.entrySet()) {
            var item = entry.getValue();
            var itemID = BuiltInRegistries.ITEM.getKey(item);
            if (!TARGET_MOD_IDS.contains(itemID.getNamespace())) continue;
            var remapped = remapItems(itemID);
            if (remapped == null) continue;
            ITEM_REMAP.put(item, remapped);
        }
    }

    private static Block remapBlocks(ResourceLocation blockLoc) {
        return switch (blockLoc.getPath()) {
            case "industrial_cogwheel" -> GearsBlocks.INDUSTRIAL_GEAR.get();
            case "large_industrial_cogwheel" -> GearsBlocks.LARGE_INDUSTRIAL_GEAR.get();
            case "cog_crank" -> GearsBlocks.COG_CRANK.get();
            case "large_cog_crank" -> GearsBlocks.LARGE_COG_CRANK.get();
            case "creative_gear_motor" -> GearsBlocks.CREATIVE_GEAR_MOTOR.get();
            default -> null;
        };
    }

    private static Item remapItems(ResourceLocation itemLoc) {
        return switch (itemLoc.getPath()) {
            case "industrial_cogwheel" -> GearsBlocks.INDUSTRIAL_GEAR.asItem();
            case "large_industrial_cogwheel" -> GearsBlocks.LARGE_INDUSTRIAL_GEAR.asItem();
            case "cog_crank" -> GearsBlocks.COG_CRANK.asItem();
            case "large_cog_crank" -> GearsBlocks.LARGE_COG_CRANK.asItem();
            case "creative_gear_motor" -> GearsBlocks.CREATIVE_GEAR_MOTOR.asItem();
            default -> null;
        };
    }
}
