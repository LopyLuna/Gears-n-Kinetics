package dev.lopyluna.gnkinetics.content.blocks.kinetics.worm_gear;

import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.gantry.GantryShaftBlock.Part;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.placement.PoleHelper;
import dev.lopyluna.gnkinetics.register.GearsBETypes;
import dev.lopyluna.gnkinetics.register.GearsBlocks;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.createmod.catnip.placement.PlacementOffset;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
public class WormGearBlock extends DirectionalKineticBlock implements IBE<WormGearBE> {
    public static final Property<Part> PART = EnumProperty.create("part", Part.class);
    private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());
    private static final int placementHelperLargeCogId = PlacementHelpers.register(new PlacementHelperLargeCog());

    public WormGearBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(PART, Part.SINGLE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder< Block, BlockState > builder) {
        super.createBlockStateDefinition(builder.add(PART));
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        var placementHelper = PlacementHelpers.get(placementHelperId);
        var placementHelperLargeCog = PlacementHelpers.get(placementHelperLargeCogId);
        IPlacementHelper placer = null;
        if (placementHelper.matchesItem(stack)) placer = placementHelper;
        if (placementHelperLargeCog.matchesItem(stack)) placer = placementHelperLargeCog;
        if (placer == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        return placer.getOffset(player, level, state, pos, hitResult).placeInWorld(level, ((BlockItem) stack.getItem()), player, hand, hitResult);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AllShapes.EIGHT_VOXEL_POLE.get(state.getValue(FACING).getAxis());
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public @NotNull BlockState updateShape(BlockState state, Direction direction, BlockState neighbour, LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        var facing = state.getValue(FACING);
        var axis = facing.getAxis();
        if (direction.getAxis() != axis) return state;
        var connect = GearsBlocks.WORM_GEAR.has(neighbour) && neighbour.getValue(FACING) == facing;
        var part = state.getValue(PART);
        if (direction.getAxisDirection() == facing.getAxisDirection()) {
            if (connect) switch (part) {
                case END -> part = Part.MIDDLE;
                case SINGLE -> part = Part.START;
            } else switch (part) {
                case MIDDLE -> part = Part.END;
                case START -> part = Part.SINGLE;
            }
        } else {
            if (connect) switch (part) {
                case START -> part = Part.MIDDLE;
                case SINGLE -> part = Part.END;
            } else switch (part) {
                case MIDDLE -> part = Part.START;
                case END -> part = Part.SINGLE;
            }
        }
        return state.setValue(PART, part);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var state = super.getStateForPlacement(context);
        if (state == null) return null;
        var pos = context.getClickedPos();
        var level = context.getLevel();
        var face = context.getClickedFace();

        var neighbour = level.getBlockState(pos.relative(state.getValue(FACING).getOpposite()));
        var clickedState = GearsBlocks.WORM_GEAR.has(neighbour) ? neighbour : level.getBlockState(pos.relative(face.getOpposite()));

        if (GearsBlocks.WORM_GEAR.has(clickedState) && clickedState.getValue(FACING).getAxis() == state.getValue(FACING).getAxis()) {
            var facing = clickedState.getValue(FACING);
            state = state.setValue(FACING, context.getPlayer() == null || !context.getPlayer().isShiftKeyDown() ? facing : facing.getOpposite());
        }
        return state;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        var onWrenched = super.onWrenched(state, context);
        if (onWrenched.consumesAction()) {
            var pos = context.getClickedPos();
            var level = context.getLevel();
            neighborChanged(level.getBlockState(pos), level, pos, state.getBlock(), pos, false);
        }
        return onWrenched;
    }

    @Override
    public boolean hasShaftTowards(LevelReader reader, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(FACING).getAxis();
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public float getParticleTargetRadius() {
        return .35f;
    }

    @Override
    public float getParticleInitialRadius() {
        return .25f;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public Class<WormGearBE> getBlockEntityClass() {
        return WormGearBE.class;
    }

    @Override
    public BlockEntityType<? extends WormGearBE> getBlockEntityType() {
        return GearsBETypes.WORM_GEAR.get();
    }

    public static class PlacementHelper extends PoleHelper<Direction> {
        public PlacementHelper() {
            super(GearsBlocks.WORM_GEAR::has, s -> s.getValue(FACING).getAxis(), FACING);
        }
        @Override
        public @NotNull Predicate<ItemStack> getItemPredicate() {
            return GearsBlocks.WORM_GEAR::isIn;
        }
    }

    public static class PlacementHelperLargeCog implements IPlacementHelper {
        @Override
        public @NotNull Predicate<ItemStack> getItemPredicate() {
            return ((Predicate<ItemStack>) ICogWheel::isLargeCogItem).and(ICogWheel::isDedicatedCogItem);
        }

        @Override
        public @NotNull Predicate<BlockState> getStatePredicate() {
            return GearsBlocks.WORM_GEAR::has;
        }

        @Override
        public @NotNull PlacementOffset getOffset(Player player, Level level, BlockState state, BlockPos pos, BlockHitResult ray) {
            var face = ray.getDirection();
            var faceAxis = face.getAxis();
            for (var dir : IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getLocation(), faceAxis)) {
                var axis = dir.getAxis();
                if (axis == state.getValue(FACING).getAxis() || axis == faceAxis) continue;
                var newPos = pos.relative(dir);
                if (!CogWheelBlock.isValidCogwheelPosition(false, level, newPos, axis)) continue;
                if (!level.getBlockState(newPos).canBeReplaced()) continue;
                return PlacementOffset.success(newPos, s -> s.setValue(BlockStateProperties.AXIS, faceAxis));
            }
            return PlacementOffset.fail();
        }
    }
}