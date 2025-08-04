package dev.lopyluna.gnkinetics.register;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.ItemLike;

@SuppressWarnings("unused")
public class GearsItems {

    //MATERIALS


    protected static String getItemName(ItemLike pItemLike) {
        return BuiltInRegistries.ITEM.getKey(pItemLike.asItem()).getPath();
    }

    public static void register() {}
}
