package dev.lopyluna.gnkinetics.mixins;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import dev.lopyluna.gnkinetics.register.GearsRotationPropagation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = KineticBlockEntity.class, remap = false)
public class KineticBlockEntityMixin {
    @Inject(method = "propagateRotationTo", at = @At("RETURN"), cancellable = true)
    public void propagateRotationTo(KineticBlockEntity target, BlockState stateFrom, BlockState stateTo, BlockPos diff, boolean connectedViaAxes, boolean connectedViaCogs, CallbackInfoReturnable<Float> cir) {
        var value = GearsRotationPropagation.propagateRotationTo(target, target.getBlockState(), stateFrom, stateTo, diff, connectedViaAxes, connectedViaCogs);
        if (value != null) cir.setReturnValue(value);
    }
}
