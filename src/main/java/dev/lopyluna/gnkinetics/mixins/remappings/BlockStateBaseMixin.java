package dev.lopyluna.gnkinetics.mixins.remappings;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.lopyluna.gnkinetics.content.utils.GearsRemapper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
    @ModifyReturnValue(method = "getBlock", at = @At("RETURN"))
    private Block remapBlock(Block original) {
        return GearsRemapper.remapBlock(original);
    }
}