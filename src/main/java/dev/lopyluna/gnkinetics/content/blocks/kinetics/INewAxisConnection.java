package dev.lopyluna.gnkinetics.content.blocks.kinetics;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface INewAxisConnection {
    static boolean isValidNewShaft(BlockState state) {
        return state.getBlock() instanceof INewAxisConnection connection && connection.hasNewShaft(state);
    }

    boolean hasNewShaft(BlockState state);

    boolean hasNewShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face);

    @Nullable Direction.Axis getNewRotationAxis(BlockState state);
}
