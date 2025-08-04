package dev.lopyluna.gnkinetics.mixins.remappings;

import dev.lopyluna.gnkinetics.content.utils.GearsRemapper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow public abstract boolean isEmpty();
    @Inject(method = "getItem", at = @At("RETURN"), cancellable = true)
    private void remapGetItem(CallbackInfoReturnable<Item> cir) {
        if (isEmpty()) return;
        var original = cir.getReturnValue();
        var remapped = GearsRemapper.remapItem(original);
        if (!remapped.equals(original)) cir.setReturnValue(remapped);
    }

}
