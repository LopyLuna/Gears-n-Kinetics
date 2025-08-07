package dev.lopyluna.gnkinetics.content.blocks.kinetics.ring_gear;

import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.custom_cogs.CustomCogWheelBlock;
import dev.lopyluna.gnkinetics.register.GearsBETypes;
import dev.lopyluna.gnkinetics.register.GearsBlocks;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
public class RingGearBlock extends RotatedPillarKineticBlock implements IBE<RingGearBE>, ICogWheel {
    public static final BooleanProperty HAS_SHAFT = CustomCogWheelBlock.HAS_SHAFT;
    public static final BooleanProperty EXTENSION = BooleanProperty.create("extension");

    public RingGearBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(HAS_SHAFT, false).setValue(EXTENSION, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(HAS_SHAFT).add(EXTENSION));
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return InteractionResult.PASS;
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return context instanceof EntityCollisionContext eContext && eContext.getEntity() != null ? Block.box(8, 8, 8, 8.001, 8.001, 8.001) : Shapes.block();
    }

    protected @NotNull VoxelShape getBlockSupportShape(BlockState state, BlockGetter reader, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    protected boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    @Override
    protected boolean isOcclusionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    @Override
    protected @NotNull VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    public Direction.Axis getAxisForPlacement(BlockPlaceContext context) {
        return Objects.requireNonNull(super.getStateForPlacement(context)).getValue(AXIS);
    }

    public static boolean canPlace(Direction.Axis axis, BlockPos pos, Level level) {
        for (var face : Iterate.directions) for (var dir : Iterate.directions) {
            if (face.getAxis() == axis || dir.getAxis() == axis || dir == face) continue;
            var state1 = level.getBlockState(pos.relative(face).relative(dir));
            if (!(state1.isEmpty() || state1.canBeReplaced())) return false;
            var state2 = level.getBlockState(pos.relative(face));
            if (!(state2.isEmpty() || state2.canBeReplaced())) return false;
        }
        return true;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var stateForPlacement = super.getStateForPlacement(context);
        assert stateForPlacement != null;
        var pos = context.getClickedPos();
        var axis = stateForPlacement.getValue(AXIS);

        for (int x = -1; x <= 1; x++) for (int y = -1; y <= 1; y++) for (int z = -1; z <= 1; z++) {
            if (axis.choose(x, y, z) != 0) continue;
            var offset = new BlockPos(x, y, z);
            if (offset.equals(BlockPos.ZERO)) continue;
            var occupiedState = context.getLevel().getBlockState(pos.offset(offset));
            if (!occupiedState.canBeReplaced()) return null;
        }

        if (context.getLevel().getBlockState(pos.relative(Direction.fromAxisAndDirection(axis, Direction.AxisDirection.NEGATIVE))).is(this))
            stateForPlacement = stateForPlacement.setValue(EXTENSION, true);
        return stateForPlacement;
    }

    public @NotNull BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos) {
        if (pDirection != Direction.fromAxisAndDirection(pState.getValue(AXIS), Direction.AxisDirection.NEGATIVE)) return pState;
        return pState.setValue(EXTENSION, pNeighborState.is(this));
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.getBlockTicks().hasScheduledTick(pos, this)) level.scheduleTick(pos, this, 1);
    }

    @Override
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        var axis = pState.getValue(AXIS);
        for (var side : Iterate.directions) {
            if (side.getAxis() == axis) continue;
            for (var secondary : Iterate.falseAndTrue) {
                var targetSide = secondary ? side.getClockWise(axis) : side;
                var structurePos = (secondary ? pPos.relative(side) : pPos).relative(targetSide);
                var occupiedState = pLevel.getBlockState(structurePos);
                var requiredStructure = GearsBlocks.RING_GEAR_STRUCT.getDefaultState().setValue(RingGearStructure.FACING, targetSide.getOpposite());
                if (occupiedState == requiredStructure) continue;
                if (!occupiedState.canBeReplaced()) {
                    pLevel.destroyBlock(pPos, false);
                    return;
                }
                pLevel.setBlockAndUpdate(structurePos, requiredStructure);
            }
        }
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return state.getValue(HAS_SHAFT) && face.getAxis() == getRotationAxis(state);
    }

    @Override
    public Class<RingGearBE> getBlockEntityClass() {
        return RingGearBE.class;
    }

    @Override
    public BlockEntityType<? extends RingGearBE> getBlockEntityType() {
        return GearsBETypes.RING_GEAR.get();
    }

    @Override
    protected @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState pState) {
        return PushReaction.BLOCK;
    }

    @Override
    public float getParticleTargetRadius() {
        return 2;
    }

    @Override
    public float getParticleInitialRadius() {
        return 1.5f;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    public boolean isLargeCog() {
        return true;
    }

    @Override
    public boolean isSmallCog() {
        return false;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }
}
