package dev.lopyluna.gnkinetics.content.blocks.kinetics.custom_cogs;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.AbstractSimpleShaftBlock;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.content.kinetics.speedController.SpeedControllerBlock;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.tterrag.registrate.util.entry.BlockEntry;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.INewAxisConnection;
import dev.lopyluna.gnkinetics.register.GearsBETypes;
import dev.lopyluna.gnkinetics.register.GearsBlocks;
import dev.lopyluna.gnkinetics.register.GearsShapes;
import net.createmod.catnip.data.Iterate;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CustomCogWheelBlock extends AbstractSimpleShaftBlock implements ICogWheel, INewAxisConnection {
    public static final BooleanProperty HAS_SHAFT =  BooleanProperty.create("has_shaft");

    boolean isThick;
    boolean isLarge;
    CogType cogType;
    public PartialModel model;

    public CustomCogWheelBlock(boolean isLarge, boolean isThick, CogType cogType, @NotNull PartialModel model, Properties properties) {
        super(properties);
        this.isLarge = isLarge;
        this.cogType = cogType;
        this.model = model;
        this.isThick = isThick;
        registerDefaultState(defaultBlockState().setValue(HAS_SHAFT, false));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(HAS_SHAFT));
    }

    public static CustomCogWheelBlock small(PartialModel model, Properties properties) {
        return new CustomCogWheelBlock(false, false, CogType.SOLID, model, properties);
    }

    public static CustomCogWheelBlock large(PartialModel model, Properties properties) {
        return new CustomCogWheelBlock(true, false, CogType.SOLID, model, properties);
    }

    public static CustomCogWheelBlock smallHollowed(PartialModel model, Properties properties) {
        return new CustomCogWheelBlock(false, false, CogType.HOLLOWED, model, properties);
    }

    public static CustomCogWheelBlock largeHollowed(PartialModel model, Properties properties) {
        return new CustomCogWheelBlock(true, false, CogType.HOLLOWED, model, properties);
    }

    public static CustomCogWheelBlock smallShaftless(PartialModel model, Properties properties) {
        return new CustomCogWheelBlock(false, false, CogType.SHAFTLESS, model, properties);
    }

    public static CustomCogWheelBlock largeShaftless(PartialModel model, Properties properties) {
        return new CustomCogWheelBlock(true, false, CogType.SHAFTLESS, model, properties);
    }


    public static CustomCogWheelBlock smallThick(PartialModel model, Properties properties) {
        return new CustomCogWheelBlock(false, true, CogType.SOLID, model, properties);
    }

    public static CustomCogWheelBlock largeThick(PartialModel model, Properties properties) {
        return new CustomCogWheelBlock(true, true, CogType.SOLID, model, properties);
    }

    @SuppressWarnings("unused")
    public static CustomCogWheelBlock smallHollowedThick(PartialModel model, Properties properties) {
        return new CustomCogWheelBlock(false, true, CogType.HOLLOWED, model, properties);
    }

    @SuppressWarnings("unused")
    public static CustomCogWheelBlock largeHollowedThick(PartialModel model, Properties properties) {
        return new CustomCogWheelBlock(true, true, CogType.HOLLOWED, model, properties);
    }

    public static CustomCogWheelBlock smallShaftlessThick(PartialModel model, Properties properties) {
        return new CustomCogWheelBlock(false, true, CogType.SHAFTLESS, model, properties);
    }

    public static CustomCogWheelBlock largeShaftlessThick(PartialModel model, Properties properties) {
        return new CustomCogWheelBlock(true, true, CogType.SHAFTLESS, model, properties);
    }

    public boolean isHollowed() {
        return cogType == CogType.HOLLOWED;
    }

    public boolean hasShaftHollowed(BlockState state) {
        return isHollowed() && state.hasProperty(HAS_SHAFT) && state.getValue(HAS_SHAFT);
    }

    @SuppressWarnings("all")
    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var hasShaftAlt = false; //cogType.hasShaft
        if (hitOnShaft(state, context)) {
            if (hasShaftAlt || (isHollowed() && state.hasProperty(HAS_SHAFT) && state.getValue(HAS_SHAFT))) {
                var newState = hasShaftAlt ? remapCycleShaft(state) : state.cycle(HAS_SHAFT);
                if (state.equals(newState)) return super.onSneakWrenched(state, context);

                KineticBlockEntity.switchToBlockState(level, pos, Block.updateFromNeighbourShapes(newState, level, pos));
                level.levelEvent(2001, pos, Block.getId(AllBlocks.SHAFT.getDefaultState()));

                if (!hasShaftAlt) updateConnections(state, level, pos);
                return InteractionResult.SUCCESS;
            }
        } else if (isHollowed() && state.hasProperty(HAS_SHAFT) && state.getValue(HAS_SHAFT)) {
            var player = context.getPlayer();
            if (player == null) super.onSneakWrenched(state, context);
            var newState = BlockHelper.copyProperties(state, AllBlocks.SHAFT.getDefaultState());
            var creative = player.isCreative();
            if (creative || BlockHelper.findAndRemoveInInventory(newState, player, 1) != 0) {

                KineticBlockEntity.switchToBlockState(level, pos, Block.updateFromNeighbourShapes(newState, level, pos));
                level.levelEvent(2001, pos, Block.getId(state));
                if (!creative) ItemHandlerHelper.giveItemToPlayer(player, state.getBlock().asItem().getDefaultInstance());
                return InteractionResult.SUCCESS;
            }
        }
        return super.onSneakWrenched(state, context);
    }

    public BlockState remapCycleShaft(BlockState state) {
        var block = state.getBlock();
        var targetState = state;
        targetState = remapCycleShaft(GearsBlocks.COG_STONE, GearsBlocks.SHAFTLESS_COG_STONE, block, targetState);
        targetState = remapCycleShaft(GearsBlocks.ANDESITE_COG, GearsBlocks.SHAFTLESS_ANDESITE_COG, block, targetState);

        targetState = remapCycleShaft(GearsBlocks.INDUSTRIAL_GEAR, GearsBlocks.SHAFTLESS_INDUSTRIAL_GEAR, block, targetState);
        targetState = remapCycleShaft(GearsBlocks.LARGE_INDUSTRIAL_GEAR, GearsBlocks.LARGE_SHAFTLESS_INDUSTRIAL_GEAR, block, targetState);

        targetState = remapCycleShaft(GearsBlocks.BRASS_GEAR, GearsBlocks.SHAFTLESS_BRASS_GEAR, block, targetState);
        targetState = remapCycleShaft(GearsBlocks.LARGE_BRASS_GEAR, GearsBlocks.LARGE_SHAFTLESS_BRASS_GEAR, block, targetState);

        targetState = remapCycleShaft(AllBlocks.COGWHEEL, GearsBlocks.SHAFTLESS_COGWHEEL, block, targetState);
        targetState = remapCycleShaft(AllBlocks.LARGE_COGWHEEL, GearsBlocks.LARGE_SHAFTLESS_COGWHEEL, block, targetState);
        return targetState == state ? state : BlockHelper.copyProperties(state, targetState);
    }

    public BlockState remapCycleShaft(BlockEntry<? extends Block> from, BlockEntry<? extends Block> to, Block state, BlockState targetState) {
        if (from.is(state)) targetState = to.getDefaultState();
        if (to.is(state)) targetState = from.getDefaultState();
        return targetState;
    }

    protected boolean hitOnShaft(BlockState state, UseOnContext ray) {
        return AllShapes.SIX_VOXEL_POLE.get(getRotationAxis(state)).bounds()
                .inflate(0.001).contains(ray.getClickLocation().subtract(ray.getClickLocation().align(Iterate.axisSet)));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        var hasShaftAlt = !cogType.hasShaft && !isHollowed();
        if ((hasShaftAlt || (isHollowed() && state.hasProperty(HAS_SHAFT) && !state.getValue(HAS_SHAFT))) && (stack.is(AllBlocks.SHAFT.asItem()) || stack.is(AllItems.ANDESITE_ALLOY))) {
            var newState = hasShaftAlt ? remapCycleShaft(state) : isHollowed() ? state.setValue(HAS_SHAFT, true) : state;
            if (state.equals(newState)) return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
            KineticBlockEntity.switchToBlockState(level, pos, Block.updateFromNeighbourShapes(newState, level, pos));
            var sound = AllBlocks.SHAFT.getDefaultState().getSoundType(level, pos, player);
            level.playSound(player, pos, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, newState));

            if (!hasShaftAlt) updateConnections(state, level, pos);
            return ItemInteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
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
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return !isThick ?
                (cogType.hasShaft || hasShaftHollowed(state) ? isLarge ? AllShapes.LARGE_GEAR : AllShapes.SMALL_GEAR :
                        isLarge ?
                        GearsShapes.shape(GearsShapes.cuboid(0, 6, 0, 16, 10, 16)).forAxis() :
                        GearsShapes.shape(GearsShapes.cuboid(2, 6, 2, 14, 10, 14)).forAxis()).get(state.getValue(AXIS)) :

                (cogType.hasShaft || hasShaftHollowed(state) ? isLarge ?
                        GearsShapes.shape(GearsShapes.cuboid(0, 2, 0, 16, 14, 16)).add(AllShapes.SIX_VOXEL_POLE.get(Direction.Axis.Y)).forAxis() :
                        GearsShapes.shape(GearsShapes.cuboid(2, 2, 2, 14, 14, 14)).add(AllShapes.SIX_VOXEL_POLE.get(Direction.Axis.Y)).forAxis() :
                        isLarge ?
                        GearsShapes.shape(GearsShapes.cuboid(0, 2, 0, 16, 14, 16)).forAxis() :
                        GearsShapes.shape(GearsShapes.cuboid(2, 2, 2, 14, 14, 14)).forAxis()).get(state.getValue(AXIS));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return isValidCogwheelPosition(ICogWheel.isLargeCog(state), worldIn, pos, state.getValue(AXIS));
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(worldIn, pos, state, placer, stack);
        if (placer instanceof Player player) triggerShiftingGearsAdvancement(worldIn, pos, state, player);
    }

    protected void triggerShiftingGearsAdvancement(Level world, BlockPos pos, BlockState state, Player player) {
        if (world.isClientSide) return;

        var axis = state.getValue(CogWheelBlock.AXIS);
        for (var perpendicular1 : Iterate.axes) {
            if (perpendicular1 == axis) continue;

            var d1 = Direction.get(Direction.AxisDirection.POSITIVE, perpendicular1);
            for (var perpendicular2 : Iterate.axes) {
                if (perpendicular1 == perpendicular2) continue;
                if (axis == perpendicular2) continue;

                var d2 = Direction.get(Direction.AxisDirection.POSITIVE, perpendicular2);
                for (int offset1 : Iterate.positiveAndNegative) for (int offset2 : Iterate.positiveAndNegative) {
                    var connectedPos = pos.relative(d1, offset1).relative(d2, offset2);
                    var blockState = world.getBlockState(connectedPos);
                    if (!(blockState.getBlock() instanceof CogWheelBlock)) continue;
                    if (blockState.getValue(CogWheelBlock.AXIS) != axis) continue;
                    if (ICogWheel.isLargeCog(blockState) == isLarge) continue;

                    AllAdvancements.COGS.awardTo(player);
                }
            }
        }
    }

    public static boolean isValidCogwheelPosition(boolean large, LevelReader worldIn, BlockPos pos, Direction.Axis cogAxis) {
        for (var facing : Iterate.directions) {
            if (facing.getAxis() == cogAxis) continue;
            var offsetPos = pos.relative(facing);
            var blockState = worldIn.getBlockState(offsetPos);
            if (blockState.hasProperty(AXIS) && facing.getAxis() == blockState.getValue(AXIS)) continue;
            if (ICogWheel.isLargeCog(blockState) || large && ICogWheel.isSmallCog(blockState)) return false;
        }
        return true;
    }

    protected Direction.Axis getAxisForPlacement(BlockPlaceContext context) {
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) return context.getClickedFace().getAxis();
        var world = context.getLevel();
        var stateBelow = world.getBlockState(context.getClickedPos().below());

        if (AllBlocks.ROTATION_SPEED_CONTROLLER.has(stateBelow) && isLargeCog()) return stateBelow.getValue(SpeedControllerBlock.HORIZONTAL_AXIS) == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;

        var placedOnPos = context.getClickedPos().relative(context.getClickedFace().getOpposite());
        var placedAgainst = world.getBlockState(placedOnPos);

        if (ICogWheel.isSmallCog(placedAgainst) && placedAgainst.getBlock() instanceof IRotate rot) return rot.getRotationAxis(placedAgainst);

        var preferredAxis = getPreferredAxis(context);
        return preferredAxis != null ? preferredAxis : context.getClickedFace().getAxis();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var shouldWaterlog = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(AXIS, getAxisForPlacement(context)).setValue(BlockStateProperties.WATERLOGGED, shouldWaterlog);
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
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return GearsBETypes.CUSTOM_COGWHEELS.get();
    }

    @SuppressWarnings("unused")
    public void updateConnection(BlockState thisState, BlockState state, Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof KineticBlockEntity be)) return;
        if (!(state.getBlock() instanceof KineticBlock kinetic)) return;
        //if (!thisState.equals(state)) if (getRotationAxis(thisState) != kinetic.getRotationAxis(state)) return;

        if (be.hasNetwork()) be.getOrCreateNetwork().remove(be);
        be.detachKinetics();
        be.removeSource();
        level.markAndNotifyBlock(pos, level.getChunkAt(pos), state, state, 3, 512);
        if (be instanceof GeneratingKineticBlockEntity genBE) genBE.reActivateSource = true;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        updateConnections(state, level, pos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        updateConnections(state, level, pos);
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public void updateConnections(BlockState state, Level level, BlockPos pos) {
        if (!level.isClientSide && hasShaftHollowed(state)) for (var dir : Iterate.directions) {
            var offset = pos.relative(dir);
            while (INewAxisConnection.isValidNewShaft(level.getBlockState(offset))) offset = offset.relative(dir);

            var offState = level.getBlockState(offset);
            if (offState.getBlock() instanceof IRotate rot && rot.hasShaftTowards(level, offset, offState, dir.getOpposite()) && level.isLoaded(offset)) {
                updateConnection(state, offState, level, offset);
            }
        }
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return cogType.hasShaft && super.hasShaftTowards(world, pos, state, face);
    }

    @Override
    public boolean hasNewShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return isHollowed() && face.getAxis() == state.getValue(AXIS);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    public boolean hasNewShaft(BlockState state) {
        return hasShaftHollowed(state);
    }

    @Override
    public @Nullable Direction.Axis getNewRotationAxis(BlockState state) {
        return hasShaftHollowed(state) ? state.getValue(AXIS) : null;
    }
}
