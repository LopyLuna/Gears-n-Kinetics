package dev.lopyluna.gnkinetics.mixins.remappings;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.lopyluna.gnkinetics.content.utils.GearsRemapper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Item.class)
public abstract class ItemMixin {
    @ModifyReturnValue(method = "getDefaultInstance", at = @At("RETURN"))
    private ItemStack remapDefaultInstance(ItemStack original) {
        return GearsRemapper.remapItemStack(original);
    }
}
