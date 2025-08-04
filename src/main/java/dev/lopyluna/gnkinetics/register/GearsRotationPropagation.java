package dev.lopyluna.gnkinetics.register;

import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.INewAxisConnection;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.tiny_cog.TinyCogBlock;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;

import static com.simibubi.create.content.contraptions.gantry.GantryCarriageBlockEntity.getGantryPinionModifier;

@SuppressWarnings("unused")
public class GearsRotationPropagation {

    public static Float propagateRotationTo(KineticBlockEntity target, BlockState targetState, BlockState stateFrom, BlockState stateTo, BlockPos diff, boolean connectedViaAxes, boolean connectedViaCogs) {
        var isLargeFrom = ICogWheel.isLargeCog(stateFrom);
        var isLargeTo = ICogWheel.isLargeCog(stateTo);

        if (isSamePlaneAndAxis(stateFrom, stateTo, diff)) {
            var axis = getAxis(stateFrom);
            for (var dir : Iterate.directions) if (dir.getAxis() != axis) {
                var dif = diff.get(dir.getAxis());
                if (Mth.abs(dif) == 2 && ICogWheel.isSmallCog(stateFrom) && stateTo.is(GearsBlocks.RING_GEAR)) return -0.25f;
                if (Mth.abs(dif) == 2 && ICogWheel.isSmallCog(stateTo) && stateFrom.is(GearsBlocks.RING_GEAR)) return -4f;

                var f = dir.getNormal().equals(diff);
                if (f && isLargeFrom && TinyCogBlock.isTinyCog(stateTo)) return -4f;
                if (f && isLargeTo && TinyCogBlock.isTinyCog(stateFrom)) return -0.25f;
            }
        }

        for (var dir : Iterate.directions) if (dir.getNormal().equals(diff)) {
            var axis = dir.getAxis();
            var axisFrom = getAxis(stateFrom);
            var axisTo = getAxis(stateTo);
            if (axisFrom == axis || axisTo == axis) break;
            var flag = axisFrom == axisTo;

            if (isLargeFrom && stateTo.is(GearsBlocks.WORM_GEAR)) return flag ? -4f : getGantryPinionModifier(stateTo.getValue(BlockStateProperties.FACING), dir) * 4;
            if (isLargeTo && stateFrom.is(GearsBlocks.WORM_GEAR)) return flag ? -0.25f : getGantryPinionModifier(stateFrom.getValue(BlockStateProperties.FACING), dir.getOpposite()) / 4;
        }

        return null;
    }

    public static void addPropagationLocations(KineticBlockEntity be, Level level, IRotate block, BlockState state, BlockPos pos, Direction.Axis axis, List<BlockPos> neighbours) {
        if (axis != null && ICogWheel.isSmallCog(state)) for (var dir : Iterate.directions) if (dir.getAxis() != axis) neighbours.add(pos.relative(dir, 2));

        if (!INewAxisConnection.isValidNewShaft(state)) {
            for (var facing : Iterate.directions) {
                var offset = pos.relative(facing);
                var offState = level.getBlockState(offset);
                while (INewAxisConnection.isValidNewShaft(level.getBlockState(offset))) offset = offset.relative(facing);
                if (offState.getBlock() instanceof INewAxisConnection newCon &&
                        newCon.hasNewShaft(offState) &&
                        newCon.hasNewShaftTowards(level, offset, offState, facing.getOpposite()) &&
                        block.hasShaftTowards(level, pos, state, facing) &&
                        level.isLoaded(offset)
                ) {
                    neighbours.add(offset);
                }
            }
        }
    }

    public static void removePropagationLocations(KineticBlockEntity be, Level level, IRotate block, BlockState state, BlockPos pos, Direction.Axis axis, List<BlockPos> neighbours) {
        if (!INewAxisConnection.isValidNewShaft(state)) {
            for (var facing : Iterate.directions) {
                var offset = pos.relative(facing);
                var offState = level.getBlockState(offset);
                if (offState.getBlock() instanceof INewAxisConnection newCon &&
                        newCon.hasNewShaft(offState) &&
                        newCon.hasNewShaftTowards(level, offset, offState, facing.getOpposite()) &&
                        block.hasShaftTowards(level, pos, state, facing) &&
                        level.isLoaded(offset)
                ) {
                    neighbours.add(offset);
                }
            }
        }
    }

    public static int isSamePlaneAndAxisOrDir(BlockState stateFrom, BlockState stateTo, BlockPos diff) {
        int i = 1;
        var axisF = getAxis(stateFrom);
        if (axisF == null) return 0;
        var axisT = getAxis(stateTo);
        if (axisT == null) return 0;
        var flag = false;
        if (diff.getX() == 1 || diff.getX() == -1) {
            flag = axisT != Direction.Axis.X;
            i = diff.getX();
        } else if (diff.getY() == 1 || diff.getY() == -1) {
            flag = axisT != Direction.Axis.Y;
            i = diff.getY();
        } else if (diff.getZ() == 1 || diff.getZ() == -1) {
            flag = axisT != Direction.Axis.Z;
            i = diff.getZ();
        }
        return (axisF == axisT || flag) && diff.get(axisF) == 0 ? i : 0;
    }



    public static void register() {}

    public static boolean isSameAxis(BlockState stateFrom, BlockState stateTo) {
        var axisF = getAxis(stateFrom);
        if (axisF == null) return false;
        var axisT = getAxis(stateTo);
        if (axisT == null) return false;
        return axisF == axisT;
    }
    public static boolean isSamePlaneAndAxis(BlockState stateFrom, BlockState stateTo, BlockPos posFrom, BlockPos posTo) {
        var axisF = getAxis(stateFrom);
        if (axisF == null) return false;
        var axisT = getAxis(stateTo);
        if (axisT == null) return false;
        return axisF == axisT && posFrom.get(axisF) == posTo.get(axisT);
    }
    public static boolean isSamePlaneAndAxis(BlockState stateFrom, BlockState stateTo, BlockPos diff) {
        var axisF = getAxis(stateFrom);
        if (axisF == null) return false;
        var axisT = getAxis(stateTo);
        if (axisT == null) return false;
        return axisF == axisT && diff.get(axisF) == 0;
    }

    public static Direction.Axis getAxis(BlockState state) {
        if (state.getBlock() instanceof ICogWheel cog) return cog.getRotationAxis(state);
        if (state.getBlock() instanceof IRotate rot) return rot.getRotationAxis(state);
        if (state.hasProperty(BlockStateProperties.AXIS)) return state.getValue(BlockStateProperties.AXIS);
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) return state.getValue(BlockStateProperties.HORIZONTAL_AXIS);
        if (state.hasProperty(BlockStateProperties.FACING)) return state.getValue(BlockStateProperties.FACING).getAxis();
        if (state.hasProperty(BlockStateProperties.FACING_HOPPER)) return state.getValue(BlockStateProperties.FACING_HOPPER).getAxis();
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) return state.getValue(BlockStateProperties.HORIZONTAL_FACING).getAxis();
        if (state.hasProperty(BlockStateProperties.VERTICAL_DIRECTION)) return state.getValue(BlockStateProperties.VERTICAL_DIRECTION).getAxis();
        return null;
    }
}
