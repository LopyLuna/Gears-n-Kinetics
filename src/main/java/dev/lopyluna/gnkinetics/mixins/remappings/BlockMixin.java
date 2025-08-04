package dev.lopyluna.gnkinetics.mixins.remappings;

import dev.lopyluna.gnkinetics.content.utils.GearsRemapper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class BlockMixin {
    @Inject(method = "defaultBlockState", at = @At("RETURN"), cancellable = true)
    private void remapDefaultBlockState(CallbackInfoReturnable<BlockState> cir) {
        var original = cir.getReturnValue();
        var remapped = GearsRemapper.remapBlockState(original);
        if (!remapped.equals(original)) cir.setReturnValue(remapped);
    }
}
