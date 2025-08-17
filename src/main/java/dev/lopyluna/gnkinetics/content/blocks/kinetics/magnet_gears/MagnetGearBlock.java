package dev.lopyluna.gnkinetics.content.blocks.kinetics.magnet_gears;

import com.simibubi.create.api.contraption.transformable.TransformableBlock;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.RotationPropagator;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;
import dev.lopyluna.gnkinetics.register.GearsBETypes;
import dev.lopyluna.gnkinetics.register.GearsShapes;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock.isValidCogwheelPosition;

@ParametersAreNonnullByDefault
public class MagnetGearBlock extends RotatedPillarKineticBlock implements ICogWheel, IBE<MagnetGearBE>, TransformableBlock {
    public static final BooleanProperty TOP_SHAFT = BooleanProperty.create("top_shaft");
    public static final BooleanProperty BOTTOM_SHAFT = BooleanProperty.create("bottom_shaft");
    public static final BooleanProperty POWERED =  BlockStateProperties.POWERED;

    boolean isLarge;
    protected MagnetGearBlock(boolean large, Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(TOP_SHAFT, false).setValue(BOTTOM_SHAFT, false).setValue(POWERED, false).setValue(AXIS, Direction.Axis.Y));
        isLarge = large;
    }
    public static MagnetGearBlock small(Properties properties) {
        return new MagnetGearBlock(false, properties);
    }
    public static MagnetGearBlock large(Properties properties) {
        return new MagnetGearBlock(true, properties);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        var shape = GearsShapes.shape(isLarge ? GearsShapes.cuboid(0, 4, 0, 16, 12, 16) : GearsShapes.cuboid(2, 4, 2, 14, 12, 14));
        if (state.getValue(TOP_SHAFT)) shape = shape.add(GearsShapes.shape(5, 0, 5, 11, 6, 11).forDirectional().get(Direction.DOWN));
        if (state.getValue(BOTTOM_SHAFT)) shape = shape.add(GearsShapes.shape(5, 0, 5, 11, 6, 11).forDirectional().get(Direction.UP));

        return shape.forAxis().get(state.getValue(AXIS));
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(POWERED, TOP_SHAFT, BOTTOM_SHAFT));
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return isValidCogwheelPosition(ICogWheel.isLargeCog(state), level, pos, state.getValue(AXIS));
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(AXIS) && state.getValue(face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? TOP_SHAFT : BOTTOM_SHAFT);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (level.isClientSide) return;

        boolean previouslyPowered = state.getValue(POWERED);
        if (previouslyPowered != level.hasNeighborSignal(pos)) {
            level.setBlock(pos, state.cycle(POWERED), 2 | 16);
            detachKinetics(level, pos, true);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var placedOn = context.getLevel().getBlockState(context.getClickedPos().relative(context.getClickedFace().getOpposite()));
        var stateForPlacement = super.getStateForPlacement(context);
        if (ICogWheel.isSmallCog(placedOn) && stateForPlacement != null) stateForPlacement = stateForPlacement.setValue(AXIS, ((IRotate) placedOn.getBlock()).getRotationAxis(placedOn));
        return stateForPlacement != null ? stateForPlacement.setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos())) : null;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        var axisDir = hitOnShaft(null, state, context);
        if (context.getClickedFace().getAxis() != state.getValue(AXIS) && axisDir == null) return super.onWrenched(state, context);
        var level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        var pos = context.getClickedPos();
        KineticBlockEntity.switchToBlockState(level, pos, state.cycle(hitOnShaft(axisDir == null ? context.getClickedFace().getAxisDirection() : axisDir, state, context) == Direction.AxisDirection.POSITIVE ? TOP_SHAFT : BOTTOM_SHAFT));
        IWrenchable.playRotateSound(level, pos);
        return InteractionResult.SUCCESS;
    }

    protected Direction.AxisDirection hitOnShaft(@Nullable Direction.AxisDirection axisDirection, BlockState state, UseOnContext ray) {
        var axis = state.getValue(AXIS);
        var negDir = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.NEGATIVE);
        if (state.getValue(TOP_SHAFT) && GearsShapes.shape(5, 0, 5, 11, 6, 11).forDirectional().get(negDir).bounds()
                .inflate(0.001).contains(ray.getClickLocation().subtract(ray.getClickLocation().align(Iterate.axisSet)))) return Direction.AxisDirection.POSITIVE;
        var posDir = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
        if (state.getValue(BOTTOM_SHAFT) && GearsShapes.shape(5, 0, 5, 11, 6, 11).forDirectional().get(posDir).bounds()
                .inflate(0.001).contains(ray.getClickLocation().subtract(ray.getClickLocation().align(Iterate.axisSet)))) return Direction.AxisDirection.NEGATIVE;

        return axisDirection;
    }


    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        originalState = swapShaftsForRotation(originalState, Rotation.CLOCKWISE_90, targetedFace.getAxis());
        return originalState.setValue(RotatedPillarKineticBlock.AXIS, VoxelShaper.axisAsFace(originalState.getValue(RotatedPillarKineticBlock.AXIS)).getClockWise(targetedFace.getAxis()).getAxis());
    }

    @Override
    public boolean isLargeCog() {
        return isLarge;
    }

    @Override
    public boolean isSmallCog() {
        return !isLarge;
    }


    @Override
    public float getParticleTargetRadius() {
        return isLargeCog() ? 1.125f : .65f;
    }

    @Override
    public float getParticleInitialRadius() {
        return isLargeCog() ? 1f : .75f;
    }

    @Override
    public boolean isDedicatedCogWheel() {
        return true;
    }

    @Override
    public Class<MagnetGearBE> getBlockEntityClass() {
        return MagnetGearBE.class;
    }

    @Override
    public BlockEntityType<? extends MagnetGearBE> getBlockEntityType() {
        return isLarge ? GearsBETypes.LARGE_MAGNET_GEAR.get() : GearsBETypes.MAGNET_GEAR.get();
    }

    public void detachKinetics(Level level, BlockPos pos, boolean reAttachNextTick) {
        if (!(level.getBlockEntity(pos) instanceof KineticBlockEntity be)) return;
        RotationPropagator.handleRemoved(level, pos, be);

        if (reAttachNextTick) level.scheduleTick(pos, this, 1, TickPriority.EXTREMELY_HIGH);
    }
    @Override
    public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
        if (!( worldIn.getBlockEntity(pos) instanceof KineticBlockEntity kte)) return;
        RotationPropagator.handleAdded(worldIn, pos, kte);
    }

    @Override
    protected boolean areStatesKineticallyEquivalent(BlockState oldState, BlockState newState) {
        if (newState.getBlock() instanceof MagnetGearBlock && oldState.getBlock() instanceof MagnetGearBlock) {
            if (newState.getValue(TOP_SHAFT) != oldState.getValue(TOP_SHAFT)) return false;
            if (newState.getValue(BOTTOM_SHAFT) != oldState.getValue(BOTTOM_SHAFT)) return false;
        }
        return super.areStatesKineticallyEquivalent(oldState, newState);
    }

    public BlockState swapShafts(BlockState state) {
        boolean bottom = state.getValue(BOTTOM_SHAFT);
        boolean top = state.getValue(TOP_SHAFT);
        state = state.setValue(BOTTOM_SHAFT, top);
        state = state.setValue(TOP_SHAFT, bottom);
        return state;
    }

    public BlockState swapShaftsForRotation(BlockState state, Rotation rotation, Direction.Axis rotationAxis) {
        if (rotation == Rotation.NONE) return state;
        var axis = state.getValue(AXIS);
        if (axis == rotationAxis) return state;
        if (rotation == Rotation.CLOCKWISE_180) return swapShafts(state);
        boolean clockwise = rotation == Rotation.CLOCKWISE_90;
        switch (rotationAxis) {
            case X -> { if (axis == Direction.Axis.Z && !clockwise || axis == Direction.Axis.Y && clockwise) return swapShafts(state); }
            case Y -> { if (axis == Direction.Axis.X && !clockwise || axis == Direction.Axis.Z && clockwise) return swapShafts(state); }
            case Z -> { if (axis == Direction.Axis.Y && !clockwise || axis == Direction.Axis.X && clockwise) return swapShafts(state); }
        }
        return state;
    }

    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        Direction.Axis axis = state.getValue(AXIS);
        if (axis == Direction.Axis.X && mirror == Mirror.FRONT_BACK || axis == Direction.Axis.Z && mirror == Mirror.LEFT_RIGHT) return swapShafts(state);
        return state;
    }

    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        state = swapShaftsForRotation(state, rotation, Direction.Axis.Y);
        return super.rotate(state, rotation);
    }

    @Override
    public BlockState transform(BlockState state, StructureTransform transform) {
        if (transform.mirror != null) state = mirror(state, transform.mirror);
        if (transform.rotationAxis == Direction.Axis.Y) return rotate(state, transform.rotation);

        state = swapShaftsForRotation(state, transform.rotation, transform.rotationAxis);
        state = state.setValue(AXIS, transform.rotateAxis(state.getValue(AXIS)));
        return state;
    }
}
