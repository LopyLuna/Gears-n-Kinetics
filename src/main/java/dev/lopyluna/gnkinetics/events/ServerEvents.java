package dev.lopyluna.gnkinetics.events;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.utility.BlockHelper;
import dev.lopyluna.gnkinetics.Gears;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.custom_cogs.CustomCogWheelBlock;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.ring_gear.RingGearBlock;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.tiny_cog.TinyCogBlock;
import dev.lopyluna.gnkinetics.register.GearsBlocks;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.createmod.catnip.placement.PlacementOffset;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

import static com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock.AXIS;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

@SuppressWarnings("removal")
@EventBusSubscriber(modid = Gears.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ServerEvents {
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        Gears.LOGGER.info(Gears.NAME + " SERVER SETUP");
    }

    private static final int ringGearLargeCogHelperId = PlacementHelpers.register(new RingGearLargeCogHelper());
    private static final int ringGearSmallCogHelperId = PlacementHelpers.register(new RingGearSmallCogHelper());
    private static final int tinyCogLargeCogHelperId = PlacementHelpers.register(new TinyCogLargeCogHelper());

    @SubscribeEvent
    public static void useItemOn(UseItemOnBlockEvent event) {
        var player = event.getPlayer();
        if (player == null) return;
        var stack = event.getItemStack();
        var level = event.getLevel();
        var pos = event.getPos();
        var state = level.getBlockState(pos);
        var hand = event.getHand();
        var context = event.getUseOnContext();
        var hitResult = new BlockHitResult(context.getClickLocation(), context.getClickedFace(), pos, context.isInside());
        var phase = event.getUsePhase();
        var shift = player.isShiftKeyDown();
        if (phase == UseItemOnBlockEvent.UsePhase.ITEM_BEFORE_BLOCK) {
            if (!shift && stack.getItem() instanceof BlockItem item && item.getBlock() instanceof CustomCogWheelBlock block && block.isHollowed() && state.is(AllBlocks.SHAFT)) {
                if (!CogWheelBlock.isValidCogwheelPosition(block.isLargeCog(), level, pos, state.getValue(AXIS))) return;
                var newState = BlockHelper.copyProperties(state, block.defaultBlockState()).setValue(CustomCogWheelBlock.HAS_SHAFT, true);
                var sound = newState.getSoundType(level, pos, player);
                level.playSound(player, pos, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
                KineticBlockEntity.switchToBlockState(level, pos, Block.updateFromNeighbourShapes(newState, level, pos));
                level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, newState));
                if (!player.isCreative()) {
                    stack.shrink(1);
                    ItemHandlerHelper.giveItemToPlayer(player, AllBlocks.SHAFT.asStack());
                }
                event.cancelWithResult(ItemInteractionResult.SUCCESS);
            }
        }
        if (phase == UseItemOnBlockEvent.UsePhase.BLOCK) {
            if (!shift && stack.getItem() instanceof BlockItem item && item.getBlock() instanceof CustomCogWheelBlock block && block.isHollowed() && state.is(AllBlocks.SHAFT)) {
                if (!CogWheelBlock.isValidCogwheelPosition(block.isLargeCog(), level, pos, state.getValue(AXIS))) return;
                event.cancelWithResult(ItemInteractionResult.SUCCESS);
                return;
            }

            IPlacementHelper placer = null;
            if (ICogWheel.isLargeCog(state)) {
                var ringGear = PlacementHelpers.get(ringGearLargeCogHelperId);
                var tinyCog = PlacementHelpers.get(tinyCogLargeCogHelperId);
                if (ringGear.matchesItem(stack)) placer = ringGear;
                if (tinyCog.matchesItem(stack)) placer = tinyCog;
            }
            if (ICogWheel.isSmallCog(state)) {
                var ringGear = PlacementHelpers.get(ringGearSmallCogHelperId);
                if (ringGear.matchesItem(stack)) placer = ringGear;
            }
            if (placer != null) event.cancelWithResult(placer.getOffset(player, level, state, pos, hitResult).placeInWorld(level, (BlockItem) stack.getItem(), player, hand, hitResult));
        }
    }

    @MethodsReturnNonnullByDefault
    private static class TinyCogLargeCogHelper implements IPlacementHelper {
        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return i -> TinyCogBlock.isTinyCog(i) || i.is(GearsBlocks.WORM_GEAR.asItem());
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return ICogWheel::isLargeCog;
        }

        @Override
        public PlacementOffset getOffset(@NotNull Player player, @NotNull Level level, BlockState state, @NotNull BlockPos pos, @NotNull BlockHitResult ray) {
            if (state.getBlock() instanceof IRotate rot) {
                var axis = rot.getRotationAxis(state);
                for (var dir : IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getLocation(), axis)) {
                    var newPos = pos.relative(dir);

                    if (!level.getBlockState(newPos).canBeReplaced()) continue;
                    return PlacementOffset.success(newPos, s -> s.hasProperty(AXIS) ? s.setValue(AXIS, axis) : s.hasProperty(FACING) ?
                            s.setValue(FACING, Direction.fromAxisAndDirection(axis, player.isShiftKeyDown() ? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE)) : s);

                }
            }
            return PlacementOffset.fail();
        }
    }

    @MethodsReturnNonnullByDefault
    private static class RingGearSmallCogHelper implements IPlacementHelper {
        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return i -> i.is(GearsBlocks.RING_GEAR.asItem());
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return ICogWheel::isSmallCog;
        }

        @Override
        public PlacementOffset getOffset(@NotNull Player player, @NotNull Level level, BlockState state, @NotNull BlockPos pos, @NotNull BlockHitResult ray) {
            if (state.getBlock() instanceof IRotate rot) {
                var axis = rot.getRotationAxis(state);
                for (var dir : IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getLocation(), axis)) {
                    var newPos = pos.relative(dir, 2);

                    if (!CogWheelBlock.isValidCogwheelPosition(false, level, newPos, axis)) continue;
                    if (!RingGearBlock.canPlace(axis, newPos, level)) continue;

                    if (!level.getBlockState(newPos).canBeReplaced()) continue;
                    return PlacementOffset.success(newPos, s -> s.setValue(AXIS, axis));

                }
            }
            return PlacementOffset.fail();
        }
    }

    @MethodsReturnNonnullByDefault
    private static class RingGearLargeCogHelper implements IPlacementHelper {
        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return i -> i.is(GearsBlocks.RING_GEAR.asItem());
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return ICogWheel::isLargeCog;
        }

        @Override
        public PlacementOffset getOffset(@NotNull Player player, @NotNull Level level, BlockState state, @NotNull BlockPos pos, @NotNull BlockHitResult ray) {
            if (state.getBlock() instanceof IRotate rot) {
                var axis = rot.getRotationAxis(state);
                var side = IPlacementHelper.orderedByDistanceOnlyAxis(pos, ray.getLocation(), axis).getFirst();
                for (var dir : IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getLocation(), axis)) {
                    var newPos = pos.relative(dir).relative(side);
                    if (!CogWheelBlock.isValidCogwheelPosition(true, level, newPos, dir.getAxis())) continue;
                    if (!RingGearBlock.canPlace(dir.getAxis(), newPos, level)) continue;

                    if (!level.getBlockState(newPos).canBeReplaced()) continue;
                    return PlacementOffset.success(newPos, s -> s.setValue(AXIS, dir.getAxis()));
                }
            }
            return PlacementOffset.fail();
        }
    }
}
