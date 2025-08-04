package dev.lopyluna.gnkinetics.content.blocks.kinetics.tiny_cog;

import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import dev.lopyluna.gnkinetics.register.GearsBETypes;
import dev.lopyluna.gnkinetics.register.GearsShapes;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.createmod.catnip.placement.PlacementOffset;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
public class TinyCogBlock extends RotatedPillarKineticBlock implements IBE<TinyCogBE>, ProperWaterloggedBlock {
    private static final int placementHelperID = PlacementHelpers.register(new TinyCogLargeCogHelper());
    public final boolean hasShaft;
    public TinyCogBlock(boolean hasShaft, Properties properties) {
        super(properties);
        registerDefaultState(super.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, false));
        this.hasShaft = hasShaft;
    }

    public static boolean isTinyCog(BlockState state) {
        return isTinyCog(state.getBlock());
    }

    public static boolean isTinyCog(Block block) {
        return block instanceof TinyCogBlock;
    }

    public static boolean isTinyCog(ItemStack stack) {
        var item = stack.getItem();
        if (!(item instanceof BlockItem blockItem)) return false;
        return isTinyCog(blockItem.getBlock());
    }

    @SuppressWarnings("unused")
    public static boolean isTinyCog(Item item) {
        if (!(item instanceof BlockItem blockItem)) return false;
        return isTinyCog(blockItem.getBlock());
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        var helper = PlacementHelpers.get(placementHelperID);
        if (helper.matchesItem(stack)) return helper.getOffset(player, level, state, pos, hitResult).placeInWorld(level, (BlockItem) stack.getItem(), player, hand, hitResult);
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(BlockStateProperties.WATERLOGGED));
    }

    public static TinyCogBlock shaftless(Properties properties) {
        return new TinyCogBlock(false, properties);
    }
    public static TinyCogBlock shaft(Properties properties) {
        return new TinyCogBlock(true, properties);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        var shape = GearsShapes.cuboid(3.5, 5, 3.5, 12.5, 11, 12.5);
        return hasShaft ? GearsShapes.shape(shape).add(AllShapes.SIX_VOXEL_POLE.get(Direction.Axis.Y)).forAxis().get(state.getValue(AXIS)) : GearsShapes.shape(shape).forAxis().get(state.getValue(AXIS));
    }

    protected Direction.Axis getAxisForPlacement(BlockPlaceContext context) {
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) return context.getClickedFace().getAxis();

        var level = context.getLevel();
        var placedOnPos = context.getClickedPos().relative(context.getClickedFace().getOpposite());
        var placedAgainst = level.getBlockState(placedOnPos);

        if (ICogWheel.isSmallCog(placedAgainst) && placedAgainst.getBlock() instanceof IRotate rot) return rot.getRotationAxis(placedAgainst);
        var preferredAxis = getPreferredAxis(context);
        return preferredAxis != null ? preferredAxis : context.getClickedFace().getAxis();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return withWater(defaultBlockState().setValue(AXIS, getAxisForPlacement(context)), context);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return hasShaft && face.getAxis() == getRotationAxis(state);
    }

    @Override
    protected @NotNull BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        updateWater(level, state, pos);
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public @NotNull FluidState getFluidState(BlockState state) {
        return fluidState(state);
    }

    @Override
    public Class<TinyCogBE> getBlockEntityClass() {
        return TinyCogBE.class;
    }

    @Override
    public BlockEntityType<? extends TinyCogBE> getBlockEntityType() {
        return GearsBETypes.TINY_COG.get();
    }

    @Override
    protected @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @MethodsReturnNonnullByDefault
    private static class TinyCogLargeCogHelper implements IPlacementHelper {
        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return ((Predicate<ItemStack>) ICogWheel::isLargeCogItem).and(ICogWheel::isDedicatedCogItem);
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return TinyCogBlock::isTinyCog;
        }

        @Override
        public PlacementOffset getOffset(@NotNull Player player, @NotNull Level level, BlockState state, @NotNull BlockPos pos, @NotNull BlockHitResult ray) {
            if (state.getBlock() instanceof IRotate rot) {
                var axis = rot.getRotationAxis(state);
                for (var dir : IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getLocation(), axis)) {
                    var newPos = pos.relative(dir);
                    if (!CogWheelBlock.isValidCogwheelPosition(false, level, newPos, axis)) continue;

                    if (!level.getBlockState(newPos).canBeReplaced()) continue;
                    return PlacementOffset.success(newPos, s -> s.setValue(AXIS, axis));
                }
            }
            return PlacementOffset.fail();
        }
    }
}
