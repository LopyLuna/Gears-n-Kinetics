package dev.lopyluna.gnkinetics.content.blocks.kinetics.planetary_gear;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.custom_cogs.CustomCogWheelBlock;
import dev.lopyluna.gnkinetics.register.GearsBETypes;
import dev.lopyluna.gnkinetics.register.GearsBlocks;
import dev.lopyluna.gnkinetics.register.GearsShapes;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
public class PlanetaryGearBlock extends RotatedPillarKineticBlock implements IBE<PlanetaryGearBE>, ICogWheel {
    public static final BooleanProperty HAS_SHAFT = CustomCogWheelBlock.HAS_SHAFT;
    public static final BooleanProperty EXTENSION = BooleanProperty.create("extension");

    public static final BooleanProperty POSITIVE_DIR = BooleanProperty.create("positive_dir");
    public static final IntegerProperty MODE = IntegerProperty.create("mode", 0, 2);

    public PlanetaryGearBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(HAS_SHAFT, false)
                .setValue(EXTENSION, false)
                .setValue(POSITIVE_DIR, false)
                .setValue(MODE, 0)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(POSITIVE_DIR, MODE, HAS_SHAFT, EXTENSION));
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        if (level.isClientSide) return InteractionResult.SUCCESS;
        level.setBlockAndUpdate(pos, state.cycle(MODE));
        AllSoundEvents.WRENCH_ROTATE.playOnServer(level, pos, 1, level.random.nextFloat() + .5f);
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return GearsShapes.shape(GearsShapes.cuboid(0, 4, 0, 16, 12, 16)).forAxis().get(state.getValue(PlanetaryGearBlock.AXIS));
    }

    protected @NotNull VoxelShape getBlockSupportShape(BlockState state, BlockGetter reader, BlockPos pos) {
        return Shapes.empty();
    }
    @Override protected boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }
    @Override protected boolean isOcclusionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
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

        return calculateAxisDir(stateForPlacement, axis, context.getClickedFace(), context.getNearestLookingDirection(), context.getPlayer(), false);
    }

    public static BlockState calculateAxisDir(BlockState state, Direction.Axis axis, Direction clickedFace, Direction nearestLookingDirection, @Nullable Player player, boolean invert) {
        if (!state.hasProperty(POSITIVE_DIR)) return state;
        var dir = clickedFace;
        dir = axis != dir.getAxis() ? axis.isVertical() ? nearestLookingDirection.getOpposite() : nearestLookingDirection : dir;
        var positive = dir.getOpposite().getAxisDirection() == Direction.AxisDirection.POSITIVE;
        positive = (player != null && player.isShiftKeyDown()) != positive;
        positive = invert != positive;
        return state.setValue(POSITIVE_DIR, dir.getAxis().isHorizontal() != positive);
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
                var requiredStructure = GearsBlocks.PLANETARY_GEAR_STRUCT.getDefaultState().setValue(PlanetaryGearStructure.FACING, targetSide.getOpposite());
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
        return face.getAxis() == getRotationAxis(state);
    }

    @Override
    public Class<PlanetaryGearBE> getBlockEntityClass() {
        return PlanetaryGearBE.class;
    }

    @Override
    public BlockEntityType<? extends PlanetaryGearBE> getBlockEntityType() {
        return GearsBETypes.PLANETARY_GEAR.get();
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
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }
}
