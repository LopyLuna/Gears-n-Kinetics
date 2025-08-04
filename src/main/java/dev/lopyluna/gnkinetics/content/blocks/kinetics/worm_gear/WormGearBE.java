package dev.lopyluna.gnkinetics.content.blocks.kinetics.worm_gear;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class WormGearBE extends KineticBlockEntity {
    public WormGearBE(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    protected boolean isNoisy() {
        return false;
    }
}
