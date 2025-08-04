package dev.lopyluna.gnkinetics.mixins.remappings;

import dev.lopyluna.gnkinetics.content.utils.GearsRemapper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
    @Inject(method = "getBlock", at = @At("RETURN"), cancellable = true)
    private void remapBlock(CallbackInfoReturnable<Block> cir) {
        var original = cir.getReturnValue();
        var remapped = GearsRemapper.remapBlock(original);
        if (!remapped.equals(original)) cir.setReturnValue(remapped);
    }
}