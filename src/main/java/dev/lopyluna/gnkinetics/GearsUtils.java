package dev.lopyluna.gnkinetics;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.Objects;

public class GearsUtils {

    public static String safeFullId(ItemLike registryEntry) {
        return safeFullName(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(registryEntry.asItem())));
    }

    public static String safeId(ItemLike registryEntry) {
        return safeName(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(registryEntry.asItem())));
    }

    public static String safeName(ResourceLocation id) {
        return id.getPath().replace('/', '_');
    }

    public static String safeFullName(ResourceLocation id) {
        return id.getNamespace() + ":" + id.getPath().replace('/', '_');
    }

    public static Item getResolvedItem(ResourceLocation loc) {
        var item = BuiltInRegistries.ITEM.get(loc);
        if (item == Items.AIR) System.out.println("⚠ Item not yet registered: " + loc + " is " + item);
        return item;
    }

}
