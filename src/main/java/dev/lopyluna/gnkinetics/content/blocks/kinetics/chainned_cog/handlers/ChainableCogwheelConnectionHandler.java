package dev.lopyluna.gnkinetics.content.blocks.kinetics.chainned_cog.handlers;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.simibubi.create.foundation.utility.CreateLang;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.chainned_cog.ChainableCogwheelBE;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.chainned_cog.ChainableCogwheelBlock;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.chainned_cog.packets.ChainableCogwheelConnectionPacket;
import dev.lopyluna.gnkinetics.register.GearsBlocks;
import dev.lopyluna.gnkinetics.register.GearsConfigs;
import dev.lopyluna.gnkinetics.register.GearsLang;
import dev.lopyluna.gnkinetics.register.GearsShapes;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import static com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity.getChainCost;
import static com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity.getChainsFromInventory;

@EventBusSubscriber(Dist.CLIENT)
public class ChainableCogwheelConnectionHandler {

    private static BlockPos firstPos;
    private static ResourceKey<Level> firstDim;

    public static boolean onRightClick() {
        Minecraft mc = Minecraft.getInstance();
        assert mc.level != null;
        assert mc.player != null;
        if (isntChain(mc.player.getMainHandItem())) return false;
        if (firstPos == null) return false;
        boolean missed = false;
        if (mc.hitResult instanceof BlockHitResult bhr && bhr.getType() != HitResult.Type.MISS) if (!(mc.level.getBlockEntity(bhr.getBlockPos()) instanceof ChainableCogwheelBE)) missed = true;
        if (!mc.player.isShiftKeyDown() && !missed) return false;
        firstPos = null;
        GearsLang.translate("chainable_cogwheel.selection_cleared").sendStatus(mc.player);
        return true;
    }

    @SuppressWarnings("deprecation")
    @SubscribeEvent
    public static void onItemUsedOnBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack itemStack = event.getItemStack();
        BlockPos pos = event.getPos();
        Level level = event.getLevel();
        Player player = event.getEntity();
        BlockState blockState = level.getBlockState(pos);

        if (!GearsBlocks.CHAINABLE_COGWHEEL.has(blockState)) return;
        if (isntChain(itemStack)) return;
        if (!player.mayBuild() || player instanceof FakePlayer) return;

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.CONSUME);

        if (!level.isClientSide())
            return;
        if (level.getBlockEntity(pos) instanceof ChainableCogwheelBE ccbe && ccbe.connections.size() >= GearsConfigs.server().kinetics.maxChainableCogwheelConnections.get()) {
            GearsLang.translate("chainable_cogwheel.cannot_add_more_connections").style(ChatFormatting.RED).sendStatus(player);
            return;
        }

        if (firstPos == null || firstDim != level.dimension()) {
            firstPos = pos;
            firstDim = level.dimension();
            player.swing(event.getHand());
            return;
        }

        boolean success = validateAndConnect(level, pos, player, itemStack, false);
        firstPos = null;

        if (!success) {
            AllSoundEvents.DENY.play(level, player, pos);
            return;
        }

        SoundType soundtype = Blocks.CHAIN.defaultBlockState().getSoundType();
        level.playSound(player, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
    }

    private static boolean isntChain(ItemStack itemStack) {
        return !itemStack.is(Items.CHAIN); // Replace with tag? generic renderer?
    }

    public static void clientTick() {
        var player = Minecraft.getInstance().player;
        if (firstPos == null || player == null) return;

        BlockEntity sourceLift = player.level().getBlockEntity(firstPos);

        if (firstDim != player.level().dimension() || !(sourceLift instanceof ChainableCogwheelBE)) {
            firstPos = null;
            GearsLang.translate("chainable_cogwheel.selection_cleared").sendStatus(player);
            return;
        }
        Level level = player.level();
        VoxelShape firstShape = GearsShapes.shape(GearsShapes.cuboid(-3, 6, -3, 19, 10, 19)).forAxis().get(level.getBlockState(firstPos).getValue(ChainableCogwheelBlock.AXIS));

        ItemStack stack = player.getMainHandItem();
        HitResult hitResult = Minecraft.getInstance().hitResult;

        if (isntChain(stack)) {
            stack = player.getOffhandItem();
            if (isntChain(stack)) return;
        }

        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
            Outliner.getInstance().showAABB("chain_connect", firstShape.bounds().move(firstPos)).lineWidth(1 / 16f).colored(0xFFFFFF);
            return;
        }

        BlockHitResult bhr = (BlockHitResult) hitResult;
        BlockPos pos = bhr.getBlockPos();
        BlockState hitState = level.getBlockState(pos);

        if (pos.equals(firstPos)) {
            Outliner.getInstance().showAABB("chain_connect", firstShape.bounds().move(firstPos)).lineWidth(1 / 16f).colored(0xFFFFFF);
            GearsLang.translate("chainable_cogwheel.select_second").sendStatus(player);
            return;
        }

        if (!(hitState.getBlock() instanceof ChainableCogwheelBlock)) {
            Outliner.getInstance().showAABB("chain_connect", firstShape.bounds().move(firstPos)).lineWidth(1 / 16f).colored(0xFFFFFF);
            return;
        }
        VoxelShape shape = GearsShapes.shape(GearsShapes.cuboid(-3, 6, -3, 19, 10, 19)).forAxis().get(hitState.getValue(ChainableCogwheelBlock.AXIS));

        boolean success = validateAndConnect(level, pos, player, stack, true);

        if (success) GearsLang.translate("chainable_cogwheel.valid_connection").style(ChatFormatting.GREEN).sendStatus(player);

        int color = success ? 0x95CD41 : 0xEA5C2B;

        var axis = hitState.hasProperty(ChainableCogwheelBlock.AXIS) ? hitState.getValue(ChainableCogwheelBlock.AXIS) : Direction.Axis.Y;
        Outliner.getInstance().showAABB("chain_connect", firstShape.bounds().move(firstPos)).lineWidth(1 / 16f).colored(color);
        Outliner.getInstance().showAABB("chain_connect_to", shape.bounds().move(pos)).lineWidth(1 / 16f).colored(color);

        Vec3 from = pos.getCenter();
        Vec3 to = firstPos.getCenter();

        Vec3 diff = from.subtract(to);

        if (diff.length() < 1) return;

        var nor = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE).getNormal();
        Vec3 normal = diff.cross(new Vec3(nor.getX(), nor.getY(), nor.getZ())).normalize().scale(12f/16f);

        Outliner.getInstance().showLine("chain_connect_line", from.add(normal), to.add(normal)).lineWidth(1 / 16f).colored(color);
        Outliner.getInstance().showLine("chain_connect_line_1", from.subtract(normal), to.subtract(normal)).lineWidth(1 / 16f).colored(color);

    }

    public static Direction.Axis getAxis(LevelAccessor level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return state.hasProperty(ChainableCogwheelBlock.AXIS) ? state.getValue(ChainableCogwheelBlock.AXIS) : null;
    }

    @SuppressWarnings("all")
    public static boolean validateAndConnect(LevelAccessor level, BlockPos pos, Player player, ItemStack chain, boolean simulate) {
        if (!simulate && player.isShiftKeyDown()) {
            GearsLang.translate("chainable_cogwheel.selection_cleared").sendStatus(player);
            return false;
        }

        if (pos.equals(firstPos)) return false;
        var axis = getAxis(level, pos);
        var firstAxis = getAxis(level, firstPos);
        if (axis != firstAxis) return fail("chainable_cogwheel.cannot_connect_axis");
        if (!pos.closerThan(firstPos, GearsConfigs.server().kinetics.maxChainableCogwheelConnections.get())) return fail("chainable_cogwheel.too_far");
        if (pos.closerThan(firstPos, 2.5)) return fail("chainable_cogwheel.too_close");

        Vec3 diff = Vec3.atLowerCornerOf(pos.subtract(firstPos));
        double horizontalDistance = switch (axis) {
            case X -> diff.multiply(0, 1, 1).length() - 1.5;
            case Y -> diff.multiply(1, 0, 1).length() - 1.5;
            case Z -> diff.multiply(1, 1, 0).length() - 1.5;
        };


        if (horizontalDistance <= 0 || Math.abs(diff.get(axis)) > 0) return fail("chainable_cogwheel.cannot_connect_vertically");
        if (Math.abs(diff.get(axis)) / horizontalDistance > 1) return fail("chainable_cogwheel.too_steep");

        ChainableCogwheelBlock chainConveyorBlock = GearsBlocks.CHAINABLE_COGWHEEL.get();
        ChainableCogwheelBE sourceLift = chainConveyorBlock.getBlockEntity(level, firstPos);
        ChainableCogwheelBE targetLift = chainConveyorBlock.getBlockEntity(level, pos);

        if (targetLift.connections.size() >= GearsConfigs.server().kinetics.maxChainableCogwheelLength.get()) return fail("chainable_cogwheel.cannot_add_more_connections");
        if (targetLift.connections.contains(firstPos.subtract(pos))) return fail("chainable_cogwheel.already_connected");
        if (sourceLift == null || targetLift == null) return fail("chainable_cogwheel.blocks_invalid");

        if (!player.isCreative()) {
            int chainCost = getChainCost(pos.subtract(firstPos));
            boolean hasEnough = getChainsFromInventory(player, chain, chainCost, true);
            if (simulate) BlueprintOverlayRenderer.displayChainRequirements(chain.getItem(), chainCost, hasEnough);
            if (!hasEnough) return fail("chainable_cogwheel.not_enough_chains");
        }

        if (simulate) return true;

        CatnipServices.NETWORK.sendToServer(new ChainableCogwheelConnectionPacket(firstPos, pos, chain, true));

        CreateLang.text("").sendStatus(player);
        firstPos = null;
        firstDim = null;
        return true;
    }

    private static boolean fail(String message) {
        assert Minecraft.getInstance().player != null;
        GearsLang.translate(message).style(ChatFormatting.RED).sendStatus(Minecraft.getInstance().player);
        return false;
    }
}
