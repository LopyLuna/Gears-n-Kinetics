package dev.lopyluna.gnkinetics.content.blocks.kinetics.tiny_cog;

import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import dev.lopyluna.gnkinetics.register.GearsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TinyCogBE extends KineticBlockEntity {
    public TinyCogBE(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    public boolean isBrass() {
        return getBlockState().is(GearsBlocks.TINY_BRASS_COG) || getBlockState().is(GearsBlocks.SHAFTLESS_TINY_BRASS_COG);
    }

    @Override
    protected boolean canPropagateDiagonally(IRotate block, BlockState state) {
        return state.getBlock() instanceof ICogWheel || block instanceof ICogWheel;
    }
}
