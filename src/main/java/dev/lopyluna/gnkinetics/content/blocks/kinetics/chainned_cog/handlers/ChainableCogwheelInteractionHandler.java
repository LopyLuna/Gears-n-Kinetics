package dev.lopyluna.gnkinetics.content.blocks.kinetics.chainned_cog.handlers;

import com.google.common.cache.Cache;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorShape;
import com.simibubi.create.foundation.utility.RaycastHelper;
import com.simibubi.create.foundation.utility.TickBasedCache;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.chainned_cog.packets.ChainableCogwheelConnectionPacket;
import dev.lopyluna.gnkinetics.mixins.ChainConveyorOBBAccessor;
import net.createmod.catnip.data.WorldAttached;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;

import java.util.List;
import java.util.Map;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS;

@EventBusSubscriber(Dist.CLIENT)
public class ChainableCogwheelInteractionHandler {
    public static WorldAttached<Cache<BlockPos, List<ChainConveyorShape>>> loadedChains =
            new WorldAttached<>($ -> new TickBasedCache<>(60, true));

    public static BlockPos selectedLift;
    public static float selectedChainPosition;
    public static BlockPos selectedConnection;
    public static Vec3 selectedBakedPosition;
    public static ChainConveyorShape selectedShape;


    public static void clientTick() {
        if (!isActive()) {
            selectedLift = null;
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (mc.player == null) return;

        ItemStack mainHandItem = mc.player.getMainHandItem();
        boolean isWrench = AllTags.AllItemTags.CHAIN_RIDEABLE.matches(mainHandItem);
        boolean dismantling = isWrench && mc.player.isShiftKeyDown();
        double range = mc.player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 1;

        Vec3 from = RaycastHelper.getTraceOrigin(mc.player);
        Vec3 to = RaycastHelper.getTraceTarget(mc.player, range, from);
        HitResult hitResult = mc.hitResult;

        double bestDiff = Float.MAX_VALUE;
        if (hitResult != null) bestDiff = hitResult.getLocation().distanceToSqr(from);

        BlockPos bestLift = null;
        ChainConveyorShape bestShape = null;
        selectedConnection = null;

        for (var entry : loadedChains.get(mc.level).asMap().entrySet()) {
            BlockPos liftPos = entry.getKey();
            var liftState = mc.level.getBlockState(liftPos);
            if (liftState.hasProperty(AXIS) && liftState.getValue(AXIS) == Direction.Axis.Y || dismantling) for (var shape : entry.getValue()) {
                if (shape instanceof ChainConveyorShape.ChainConveyorBB && dismantling) continue;
                Vec3 liftVec = Vec3.atLowerCornerOf(liftPos);
                Vec3 intersect = shape.intersect(from.subtract(liftVec), to.subtract(liftVec));
                if (intersect == null) continue;

                double distanceToSqr = intersect.add(liftVec).distanceToSqr(from);
                if (distanceToSqr > bestDiff) continue;
                bestDiff = distanceToSqr;
                bestLift = liftPos;
                bestShape = shape;
                selectedChainPosition = shape.getChainPosition(intersect);
                if (shape instanceof ChainConveyorShape.ChainConveyorOBB obb) selectedConnection = ((ChainConveyorOBBAccessor) obb).connection();
            }
        }

        selectedLift = bestLift;
        if (bestLift == null) return;

        selectedShape = bestShape;
        selectedBakedPosition = bestShape.getVec(bestLift, selectedChainPosition);

        if (!isWrench) Outliner.getInstance()
                .chaseAABB("ChainPointSelection", new AABB(selectedBakedPosition, selectedBakedPosition))
                .colored(Color.WHITE)
                .lineWidth(1 / 6f)
                .disableLineNormals();
    }

    private static boolean isActive() {
        var mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        ItemStack mainHandItem = mc.player.getMainHandItem();
        return AllTags.AllItemTags.CHAIN_RIDEABLE.matches(mainHandItem);
    }

    public static boolean onUse() {
        if (selectedLift == null) return false;
        var mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        ItemStack mainHandItem = mc.player.getMainHandItem();

        if (AllTags.AllItemTags.CHAIN_RIDEABLE.matches(mainHandItem)) {
            if (!mc.player.isShiftKeyDown()) {
                ChainableCogwheelRidingHandler.embark(selectedLift, selectedChainPosition, selectedConnection);
                return true;
            }
            CatnipServices.NETWORK.sendToServer(new ChainableCogwheelConnectionPacket(selectedLift, selectedLift.offset(selectedConnection), mainHandItem, false));
            return true;
        }
        return true;
    }

    public static void drawCustomBlockSelection(PoseStack ms, MultiBufferSource buffer, Vec3 camera) {
        if (selectedLift == null || selectedShape == null) return;

        VertexConsumer vb = buffer.getBuffer(RenderType.lines());
        ms.pushPose();
        ms.translate(selectedLift.getX() - camera.x, selectedLift.getY() - camera.y, selectedLift.getZ() - camera.z);
        if (selectedShape instanceof ChainConveyorShape.ChainConveyorOBB obb) obb.drawOutline(selectedLift, ms, vb);
        if (selectedShape instanceof ChainConveyorShape.ChainConveyorBB bb) bb.drawOutline(selectedLift, ms, vb);
        ms.popPose();
    }

    @SubscribeEvent
    public static void hideVanillaBlockSelection(RenderHighlightEvent.Block event) {
        if (selectedLift == null || selectedShape == null) return;
        event.setCanceled(true);
    }
}
