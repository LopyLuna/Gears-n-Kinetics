package dev.lopyluna.gnkinetics.mixins.remappings;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.lopyluna.gnkinetics.content.utils.GearsRemapper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Level.class)
public class LevelMixin {
    @WrapMethod(
            method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z"
    )
    private boolean onSetBlock(BlockPos pos, BlockState state, int flags, int recursionLeft, Operation<Boolean> original) {
        var remapped = GearsRemapper.remapBlockState(state);
        return original.call(pos, remapped, flags, recursionLeft);
    }
}
