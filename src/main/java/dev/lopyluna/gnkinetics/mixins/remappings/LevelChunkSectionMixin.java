package dev.lopyluna.gnkinetics.mixins.remappings;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.lopyluna.gnkinetics.content.utils.GearsRemapper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelChunkSection.class)
public abstract class LevelChunkSectionMixin {
    @ModifyReturnValue(method = "setBlockState(IIILnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;", at = @At("RETURN"))
    private BlockState onSetBlockState(BlockState original) {
        return GearsRemapper.remapBlockState(original);
    }
}
