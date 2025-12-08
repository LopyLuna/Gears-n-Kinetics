package dev.lopyluna.gnkinetics.content.blocks.kinetics.ring_gear;

import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import dev.lopyluna.gnkinetics.register.GearsBlocks;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

import static dev.lopyluna.gnkinetics.register.GearsRotationPropagation.getAxis;
import static dev.lopyluna.gnkinetics.register.GearsRotationPropagation.isSamePlaneAndAxis;

public class RingGearBE extends KineticBlockEntity {
    public RingGearBE(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public float propagateRotationTo(KineticBlockEntity target, BlockState stateFrom, BlockState stateTo, BlockPos diff, boolean connectedViaAxes, boolean connectedViaCogs) {
        if (isSamePlaneAndAxis(stateFrom, stateTo, diff)) {
            var axis = getAxis(stateFrom);
            for (var dir : Iterate.directions) if (dir.getAxis() != axis) {
                assert target.getLevel() != null;
                var dif = diff.get(dir.getAxis());
                if (Mth.abs(dif) == 3 && (stateTo.is(GearsBlocks.RING_GEAR) || stateTo.is(GearsBlocks.PLANETARY_GEAR)) && (stateFrom.is(GearsBlocks.RING_GEAR) || stateFrom.is(GearsBlocks.PLANETARY_GEAR))) return -1f;
                if (Mth.abs(dif) == 2 && ICogWheel.isSmallCog(stateFrom) && (stateTo.is(GearsBlocks.RING_GEAR) || stateTo.is(GearsBlocks.PLANETARY_GEAR))) return -0.25f;
                if (Mth.abs(dif) == 2 && ICogWheel.isSmallCog(stateTo) && (stateFrom.is(GearsBlocks.RING_GEAR) || stateFrom.is(GearsBlocks.PLANETARY_GEAR))) return -4f;
            }
        }
        return super.propagateRotationTo(target, stateFrom, stateTo, diff, connectedViaAxes, connectedViaCogs);
    }

    @Override
    protected boolean canPropagateDiagonally(IRotate block, BlockState state) {
        return state.getBlock() instanceof ICogWheel || block instanceof ICogWheel;
    }

    @Override
    public List<BlockPos> addPropagationLocations(IRotate block, BlockState state, List<BlockPos> neighbours) {
        for (var dir : Iterate.directions) if (dir.getAxis() != state.getValue(RingGearBlock.AXIS)) {
            neighbours.add(worldPosition.relative(dir, 2));
            neighbours.add(worldPosition.relative(dir, 3));
        }
        for (var offset : BlockPos.betweenClosed(-1, -1, -1, 1, 1, 1)) if (offset.distSqr(BlockPos.ZERO) == 2) neighbours.add(worldPosition.offset(offset));
        return super.addPropagationLocations(block, state, neighbours);
    }
}
