package dev.lopyluna.gnkinetics.content.blocks.kinetics.planetary_gear;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.api.equipment.goggles.IProxyHoveringInformation;
import com.simibubi.create.content.contraptions.bearing.SailBlock;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.content.kinetics.waterwheel.WaterWheelStructuralBlock;
import com.simibubi.create.foundation.block.render.MultiPosDestructionHandler;
import dev.lopyluna.gnkinetics.register.GearsBlocks;
import dev.lopyluna.gnkinetics.register.GearsShapes;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.createmod.catnip.placement.PlacementOffset;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
public class PlanetaryGearStructure extends DirectionalBlock implements IWrenchable, IProxyHoveringInformation {
    private static final int planetaryGearHelperId = PlacementHelpers.register(new PlanetaryGearHelper());
    private static final int smallCogHelperId = PlacementHelpers.register(new SmallCogHelper());
    public static final MapCodec<PlanetaryGearStructure> CODEC = simpleCodec(PlanetaryGearStructure::new);
    public PlanetaryGearStructure(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        var planetaryGear = PlacementHelpers.get(planetaryGearHelperId);
        var smallCog = PlacementHelpers.get(smallCogHelperId);
        IPlacementHelper placer = null;
        if (planetaryGear.matchesItem(stack)) placer = planetaryGear;
        if (smallCog.matchesItem(stack)) placer = smallCog;
        if (placer != null) return placer.getOffset(player, level, state, pos, hitResult).placeInWorld(level, (BlockItem) stack.getItem(), player, hand, hitResult);
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder.add(FACING));
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        var masterPos = getMaster(level, pos, state);
        var masterState = level.getBlockState(masterPos);
        if (masterState.getBlock() instanceof PlanetaryGearBlock)
            return GearsShapes.shape(GearsShapes.cuboid(0, 4, 0, 16, 12, 16)).forAxis().get(masterState.getValue(PlanetaryGearBlock.AXIS));
        return context instanceof EntityCollisionContext eContext && eContext.getEntity() != null ? Shapes.empty() : Shapes.block();
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
    protected void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state) {
    }

    @Override
    protected @NotNull MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public PushReaction getPistonPushReaction(@NotNull BlockState pState) {
        return PushReaction.BLOCK;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return InteractionResult.PASS;
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        return GearsBlocks.PLANETARY_GEAR.asStack();
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        var clickedPos = context.getClickedPos();
        var level = context.getLevel();
        if (stillValid(level, clickedPos, state, false)) {
            var masterPos = getMaster(level, clickedPos, state);
            context = new UseOnContext(level, context.getPlayer(), context.getHand(), context.getItemInHand(),
                    new BlockHitResult(context.getClickLocation(), context.getClickedFace(), masterPos, context.isInside()));
            state = level.getBlockState(masterPos);
        }
        return IWrenchable.super.onSneakWrenched(state, context);
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (stillValid(pLevel, pPos, pState, false)) pLevel.destroyBlock(getMaster(pLevel, pPos, pState), true);
    }

    public @NotNull BlockState playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        if (stillValid(pLevel, pPos, pState, false)) {
            var masterPos = getMaster(pLevel, pPos, pState);
            pLevel.destroyBlockProgress(masterPos.hashCode(), masterPos, -1);
            if (!pLevel.isClientSide() && pPlayer.isCreative()) pLevel.destroyBlock(masterPos, false);
        }
        return super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
    }

    @Override
    public @NotNull BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        if (stillValid(pLevel, pCurrentPos, pState, false)) {
            var masterPos = getMaster(pLevel, pCurrentPos, pState);
            if (!pLevel.getBlockTicks().hasScheduledTick(masterPos, GearsBlocks.PLANETARY_GEAR.get())) pLevel.scheduleTick(masterPos, GearsBlocks.PLANETARY_GEAR.get(), 1);
            return pState;
        }
        if (!(pLevel instanceof Level level) || level.isClientSide()) return pState;
        if (!level.getBlockTicks().hasScheduledTick(pCurrentPos, this)) level.scheduleTick(pCurrentPos, this, 1);
        return pState;
    }

    public static BlockPos getMaster(BlockGetter level, BlockPos pos, BlockState state) {
        var direction = state.getValue(FACING);
        var targetedPos = pos.relative(direction);
        var targetedState = level.getBlockState(targetedPos);
        if (targetedState.is(GearsBlocks.PLANETARY_GEAR_STRUCT.get())) return getMaster(level, targetedPos, targetedState);
        return targetedPos;
    }

    public boolean stillValid(BlockGetter level, BlockPos pos, BlockState state, boolean directlyAdjacent) {
        if (!state.is(this)) return false;
        var direction = state.getValue(FACING);
        var targetedPos = pos.relative(direction);
        var targetedState = level.getBlockState(targetedPos);

        if (!directlyAdjacent && stillValid(level, targetedPos, targetedState, true)) return true;
        return targetedState.getBlock() instanceof PlanetaryGearBlock && targetedState.getValue(PlanetaryGearBlock.AXIS) != direction.getAxis();
    }

    @Override
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        if (!stillValid(pLevel, pPos, pState, false)) pLevel.setBlockAndUpdate(pPos, Blocks.AIR.defaultBlockState());
    }

    @Override
    public boolean addLandingEffects(BlockState state1, ServerLevel level, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
        return true;
    }

    public static class RenderProperties implements IClientBlockExtensions, MultiPosDestructionHandler {

        @Override
        public boolean addDestroyEffects(BlockState state, Level Level, BlockPos pos, ParticleEngine manager) {
            return true;
        }

        @Override
        public boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine manager) {
            if (target instanceof BlockHitResult bhr) {
                var targetPos = bhr.getBlockPos();
                var structure = GearsBlocks.PLANETARY_GEAR_STRUCT.get();
                if (structure.stillValid(level, targetPos, state, false)) manager.crack(getMaster(level, targetPos, state), bhr.getDirection());
                return true;
            }
            return IClientBlockExtensions.super.addHitEffects(state, level, target, manager);
        }

        @Override
        @Nullable
        public Set<BlockPos> getExtraPositions(ClientLevel level, BlockPos pos, BlockState blockState, int progress) {
            var structure = GearsBlocks.PLANETARY_GEAR_STRUCT.get();
            if (!structure.stillValid(level, pos, blockState, false)) return null;
            HashSet<BlockPos> set = new HashSet<>();
            set.add(WaterWheelStructuralBlock.getMaster(level, pos, blockState));
            return set;
        }
    }

    @Override
    public BlockPos getInformationSource(Level level, BlockPos pos, BlockState state) {
        return stillValid(level, pos, state, false) ? getMaster(level, pos, state) : pos;
    }

    @MethodsReturnNonnullByDefault
    private static class PlanetaryGearHelper implements IPlacementHelper {
        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return i -> i.is(GearsBlocks.PLANETARY_GEAR.asItem()) || i.is(GearsBlocks.RING_GEAR.asItem());
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return s -> s.getBlock() instanceof PlanetaryGearStructure;
        }

        @Override
        public PlacementOffset getOffset(Player player, Level level, BlockState state, BlockPos pos, BlockHitResult ray) {
            var dir = state.getValue(SailBlock.FACING);
            var placeDir = dir.getOpposite();
            var faceAxis = ray.getDirection().getAxis();
            var gear = level.getBlockState(pos.relative(dir));
            var placePos = pos.relative(placeDir, 2);
            var placeState = level.getBlockState(placePos);
            if ((gear.is(GearsBlocks.PLANETARY_GEAR) || gear.is(GearsBlocks.RING_GEAR)) && faceAxis != dir.getAxis()  && (placeState.isEmpty() || placeState.canBeReplaced()) && placeState.canSurvive(level, placePos) && PlanetaryGearBlock.canPlace(faceAxis, placePos, level)) {
                var axis = gear.getValue(PlanetaryGearBlock.AXIS);
                return PlacementOffset.success(placePos, s -> {
                    var newState = (PlanetaryGearBlock.calculateAxisDir(s, axis, ray.getDirection(), player.getNearestViewDirection(), player, true));
                    if (s.hasProperty(BlockStateProperties.AXIS)) newState = newState.setValue(BlockStateProperties.AXIS, axis);
                    if (s.hasProperty(BlockStateProperties.FACING)) newState = newState.setValue(BlockStateProperties.FACING, Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE));
                    return newState;
                });
            }
            return PlacementOffset.fail();
        }
    }

    @MethodsReturnNonnullByDefault
    private static class SmallCogHelper implements IPlacementHelper {
        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return ((Predicate<ItemStack>) ICogWheel::isSmallCogItem).and(ICogWheel::isDedicatedCogItem);
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return s -> s.getBlock() instanceof PlanetaryGearStructure;
        }

        @Override
        public PlacementOffset getOffset(Player player, Level level, BlockState state, BlockPos pos, BlockHitResult ray) {

            var dir = state.getValue(SailBlock.FACING);
            var placeDir = dir.getOpposite();
            var gear = level.getBlockState(pos.relative(dir));
            var placePos = pos.relative(placeDir);
            var placeState = level.getBlockState(placePos);
            if ((gear.is(GearsBlocks.PLANETARY_GEAR) || gear.is(GearsBlocks.RING_GEAR)) && (placeState.isEmpty() || placeState.canBeReplaced()) && placeState.canSurvive(level, placePos)) {
                var axis = gear.getValue(PlanetaryGearBlock.AXIS);
                return PlacementOffset.success(placePos, s -> {
                    var newState = (PlanetaryGearBlock.calculateAxisDir(s, axis, ray.getDirection(), player.getNearestViewDirection(), player, true));
                    if (s.hasProperty(BlockStateProperties.AXIS)) newState = newState.setValue(BlockStateProperties.AXIS, axis);
                    if (s.hasProperty(BlockStateProperties.FACING)) newState = newState.setValue(BlockStateProperties.FACING, Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE));
                    return newState;
                });
            }
            return PlacementOffset.fail();
        }
    }
}
