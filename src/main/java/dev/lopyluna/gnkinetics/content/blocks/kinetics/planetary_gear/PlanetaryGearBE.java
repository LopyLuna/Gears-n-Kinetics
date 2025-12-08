package dev.lopyluna.gnkinetics.content.blocks.kinetics.planetary_gear;

import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.content.kinetics.transmission.SplitShaftBlockEntity;
import dev.lopyluna.gnkinetics.register.GearsBlocks;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

import static dev.lopyluna.gnkinetics.register.GearsRotationPropagation.getAxis;
import static dev.lopyluna.gnkinetics.register.GearsRotationPropagation.isSamePlaneAndAxis;

public class PlanetaryGearBE extends SplitShaftBlockEntity {
    public PlanetaryGearBE(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public float propagateRotationTo(KineticBlockEntity target, BlockState stateFrom, BlockState stateTo, BlockPos diff, boolean connectedViaAxes, boolean connectedViaCogs) {
        if (isSamePlaneAndAxis(stateFrom, stateTo, diff)) {
            var axis = getAxis(stateFrom);
            for (var dir : Iterate.directions) if (dir.getAxis() != axis) {
                assert target.getLevel() != null;
                var dif = diff.get(dir.getAxis());
                if (Mth.abs(dif) == 3 && (stateTo.is(GearsBlocks.PLANETARY_GEAR) || stateTo.is(GearsBlocks.RING_GEAR)) && (stateFrom.is(GearsBlocks.PLANETARY_GEAR) || stateFrom.is(GearsBlocks.RING_GEAR))) return -1f;
                if (Mth.abs(dif) == 2 && ICogWheel.isSmallCog(stateFrom) && (stateTo.is(GearsBlocks.PLANETARY_GEAR) || stateTo.is(GearsBlocks.RING_GEAR))) return -0.25f;
                if (Mth.abs(dif) == 2 && ICogWheel.isSmallCog(stateTo) && (stateFrom.is(GearsBlocks.PLANETARY_GEAR) || stateFrom.is(GearsBlocks.RING_GEAR))) return -4f;
            }
        }
        return super.propagateRotationTo(target, stateFrom, stateTo, diff, connectedViaAxes, connectedViaCogs);
    }

    @Override
    public float getRotationSpeedModifier(Direction face) {
        var dir = getSourceFacing();
        var mode = getBlockState().getValue(PlanetaryGearBlock.MODE);
        var axisDir = !getBlockState().getValue(PlanetaryGearBlock.POSITIVE_DIR) ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE;
        var positive = dir.getAxisDirection() != axisDir;

        // Mode 0 - Carrier Locked
        // Mode 1 - Sun Locked
        // Mode 2 - Ring Locked

        var inputSpeed = switch (mode) {
            case 1 -> positive ? 1f : 3f / 2f;
            case 2 -> positive ? 3f : 1f;
            default -> positive ? 1f : -2f;
        };
        var outputSpeed = switch (mode) {
            case 1 -> positive ? 2f / 3f : 1f;
            case 2 -> positive ? 1f / 3f : 1f;
            default -> positive ? -2f : 1f;
        };

        if (hasSource() && face != dir) return outputSpeed;
        return inputSpeed;
    }

    @Override
    protected boolean canPropagateDiagonally(IRotate block, BlockState state) {
        return ICogWheel.isSmallCog(state) || block instanceof ICogWheel cog && cog.isSmallCog();
    }

    @Override
    public List<BlockPos> addPropagationLocations(IRotate block, BlockState state, List<BlockPos> neighbours) {
        for (var dir : Iterate.directions) if (dir.getAxis() != state.getValue(PlanetaryGearBlock.AXIS)) {
            neighbours.add(worldPosition.relative(dir, 2));
            neighbours.add(worldPosition.relative(dir, 3));
        }
        //for (var offset : BlockPos.betweenClosed(-1, -1, -1, 1, 1, 1)) if (offset.distSqr(BlockPos.ZERO) == 2) neighbours.add(worldPosition.offset(offset));
        return super.addPropagationLocations(block, state, neighbours);
    }
}
