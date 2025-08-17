package dev.lopyluna.gnkinetics.content.blocks.kinetics.magnet_gears;

import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class MagnetGearBE extends SimpleKineticBlockEntity {
    public MagnetGearBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public boolean isLarge() {
        return getBlockState().getBlock() instanceof MagnetGearBlock block && block.isLarge;
    }

    @Override
    public float propagateRotationTo(KineticBlockEntity target, BlockState stateFrom, BlockState stateTo, BlockPos diff, boolean connectedViaAxes, boolean connectedViaCogs) {
        assert level != null;
        if (target instanceof MagnetGearBE be && stateTo.getBlock() instanceof MagnetGearBlock blockTo && stateFrom.getBlock() instanceof MagnetGearBlock blockFrom) {
            var fromAxis = stateFrom.getValue(MagnetGearBlock.AXIS);
            var toAxis = stateTo.getValue(MagnetGearBlock.AXIS);
            var fromPowered = stateFrom.getValue(MagnetGearBlock.POWERED);
            var toPowered = stateTo.getValue(MagnetGearBlock.POWERED);
            if (!fromPowered && !toPowered) for (var dir : Iterate.directions) if (dir.getAxis() == fromAxis && fromAxis == toAxis) {
                if (!dir.getNormal().equals(diff)) continue;
                if (blockTo.hasShaftTowards(level, be.getBlockPos(), stateTo, dir.getOpposite()) && blockFrom.hasShaftTowards(level, worldPosition, stateFrom, dir)) continue;
                if (!isLarge() && be.isLarge()) return -0.5f;
                if (isLarge() && !be.isLarge()) return -2f;
                return -1f;
            }
        }
        return super.propagateRotationTo(target, stateFrom, stateTo, diff, connectedViaAxes, connectedViaCogs);
    }

    @Override
    protected boolean canPropagateDiagonally(IRotate block, BlockState state) {
        return state.getBlock() instanceof ICogWheel || block instanceof ICogWheel;
    }
}
