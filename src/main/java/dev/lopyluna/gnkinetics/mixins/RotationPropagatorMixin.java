package dev.lopyluna.gnkinetics.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.kinetics.RotationPropagator;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import dev.lopyluna.gnkinetics.content.client.DebugOutliner;
import dev.lopyluna.gnkinetics.register.GearsRotationPropagation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = RotationPropagator.class, remap = false)
public abstract class RotationPropagatorMixin {

    @WrapOperation(method = "getPotentialNeighbourLocations", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/base/KineticBlockEntity;addPropagationLocations(Lcom/simibubi/create/content/kinetics/base/IRotate;Lnet/minecraft/world/level/block/state/BlockState;Ljava/util/List;)Ljava/util/List;"))
    private static List<BlockPos> mergeAddPropagationLocations(KineticBlockEntity be, IRotate block, BlockState state, List<BlockPos> neighbours, Operation<List<BlockPos>> original) {
        var positions = original.call(be, block, state, neighbours);
        var level = be.getLevel();
        var pos = be.getBlockPos();
        if (level != null) {
            var axis = GearsRotationPropagation.getAxis(state);
            List<BlockPos> addition = new ArrayList<>();
            GearsRotationPropagation.addPropagationLocations(be, level, block, state, pos, axis, addition);
            positions.addAll(addition);

            List<BlockPos> removal = new ArrayList<>();
            GearsRotationPropagation.removePropagationLocations(be, level, block, state, pos, axis, removal);
            positions.removeAll(removal);
        }
        return positions;
    }

    @Shadow
    private static List<KineticBlockEntity> getConnectedNeighbours(KineticBlockEntity be) {
        throw new AssertionError();
    }
    @Shadow
    private static List<BlockPos> getPotentialNeighbourLocations(KineticBlockEntity be) {
        throw new AssertionError();
    }

    @Inject(method = "handleAdded", at = @At("TAIL"))
    private static void injectHandleAddedRendering(Level level, BlockPos pos, KineticBlockEntity addedTE, CallbackInfo ci) {
        DebugOutliner.showBlockPositionsTTL(getPotentialNeighbourLocations(addedTE), p -> p
                .colored(0xFF_ceff5d)
                .lineWidth(0.95f/16f)
                .disableCull()
                .disableLineNormals()
        );
        DebugOutliner.showBlockPositionsTTL(getConnectedNeighbours(addedTE).stream().map(BlockEntity::getBlockPos).toList(), p -> p
                .colored(0xFF_ceff5d)
                .lineWidth(0.95f/16f)
                .disableCull()
                .disableLineNormals()
        );
    }
}
