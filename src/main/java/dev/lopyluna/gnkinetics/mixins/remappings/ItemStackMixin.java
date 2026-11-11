package dev.lopyluna.gnkinetics.mixins.remappings;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.lopyluna.gnkinetics.content.utils.GearsRemapper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow public abstract boolean isEmpty();
    @ModifyReturnValue(method = "getItem", at = @At("RETURN"))
    private Item remapGetItem(Item original) {
        if (isEmpty()) return original;
        return GearsRemapper.remapItem(original);
    }

}
