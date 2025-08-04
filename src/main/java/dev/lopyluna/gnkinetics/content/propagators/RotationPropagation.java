package dev.lopyluna.gnkinetics.content.propagators;

import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public interface RotationPropagation {
    default Float propagateRotationTo(KineticBlockEntity instance, KineticBlockEntity target, BlockState stateFrom, BlockState stateTo, BlockPos diff, boolean connectedViaAxes, boolean connectedViaCogs) { return null; }
    default Float getRotationSpeedModifier(KineticBlockEntity from, KineticBlockEntity to) { return null; }
    default Float getConveyedSpeed(KineticBlockEntity from, KineticBlockEntity to) { return null; }

    default boolean propagateNewSource(KineticBlockEntity currentBE) { return false; }
    default boolean propagateMissingSource(KineticBlockEntity updateBE) { return false; }

    default boolean handleAdded(Level level, BlockPos pos, KineticBlockEntity addedBE) { return false; }
    default boolean handleRemoved(Level level, BlockPos pos, KineticBlockEntity removedBE) { return false; }

    default List<KineticBlockEntity> getConnectedNeighbours(KineticBlockEntity be, List<KineticBlockEntity> original) { return null; }
    default List<BlockPos> getPotentialNeighbourLocations(KineticBlockEntity be, List<BlockPos> original) { return null; }
    default List<BlockPos> addPropagationLocations(KineticBlockEntity instance, IRotate block, BlockState state, List<BlockPos> neighbours) { return null; }
    default List<BlockPos> addPropagationLocationsAlt(KineticBlockEntity instance, IRotate block, BlockState state) { return new ArrayList<>(); }
}
