package dev.lopyluna.gnkinetics.mixins;

import com.simibubi.create.content.decoration.bracket.BracketBlock;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.custom_cogs.CustomCogWheelBlock;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.tiny_cog.TinyCogBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(BracketBlock.class)
public abstract class BracketBlockMixin {
    @Shadow protected abstract Optional<BlockState> getSuitableBracket(Direction.Axis targetBlockAxis, Direction direction, BracketBlock.BracketType type);

    @Inject(method = "getSuitableBracket(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Ljava/util/Optional;", at = @At(value = "HEAD"), cancellable = true)
    private void modifyGetSuitableBracket(BlockState blockState, Direction direction, CallbackInfoReturnable<Optional<BlockState>> cir) {
        var block = blockState.getBlock();
        if (block instanceof CustomCogWheelBlock || block instanceof TinyCogBlock)
            cir.setReturnValue(getSuitableBracket(blockState.getValue(RotatedPillarKineticBlock.AXIS), direction, BracketBlock.BracketType.COG));
    }
}
