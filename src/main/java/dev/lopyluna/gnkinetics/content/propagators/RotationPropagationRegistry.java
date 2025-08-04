package dev.lopyluna.gnkinetics.content.propagators;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RotationPropagationRegistry {
    private static final Map<NonNullSupplier<Block>, RotationPropagation> handlers = new HashMap<>();
    private static final List<RotationPropagation> singleHandlers = new ArrayList<>();

    public static void register(NonNullSupplier<Block> block, RotationPropagation handler) {
        handlers.put(block, handler);
    }

    public static RotationPropagation register(RotationPropagation handler) {
        singleHandlers.add(handler);
        return handler;
    }

    public static RotationPropagation get(NonNullSupplier<Block> block) {
        return !handlers.isEmpty() ? handlers.get(block) : null;
    }

    public static RotationPropagation get(Block block) {
        return get(() -> block);
    }

    public static RotationPropagation get(BlockState state) {
        return get(state::getBlock);
    }

    public static RotationPropagation get(Level level, BlockPos pos) {
        return get(level.getBlockState(pos));
    }

    public static RotationPropagation get(KineticBlockEntity be) {
        return be != null ? get(be.getBlockState()) : null;
    }

    public static List<RotationPropagation> getEntries() {
        return singleHandlers;
    }
}
