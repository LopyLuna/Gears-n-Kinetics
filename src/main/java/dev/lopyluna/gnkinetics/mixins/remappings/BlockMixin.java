package dev.lopyluna.gnkinetics.mixins.remappings;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.lopyluna.gnkinetics.content.utils.GearsRemapper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Block.class)
public abstract class BlockMixin {
    @ModifyReturnValue(method = "defaultBlockState", at = @At("RETURN"))
    private BlockState remapDefaultBlockState(BlockState original) {
        return GearsRemapper.remapBlockState(original);
    }
}
