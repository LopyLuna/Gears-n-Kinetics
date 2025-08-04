package dev.lopyluna.gnkinetics.mixins.remappings;

import dev.lopyluna.gnkinetics.content.utils.GearsRemapper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin {
    @Inject(method = "getDefaultInstance", at = @At("RETURN"), cancellable = true)
    private void remapDefaultInstance(CallbackInfoReturnable<ItemStack> cir) {
        var original = cir.getReturnValue();
        var remapped = GearsRemapper.remapItemStack(original);
        if (!remapped.equals(original)) cir.setReturnValue(remapped);
    }
}
