package dev.lopyluna.gnkinetics.content.blocks.kinetics.chainned_cog.handlers;

import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.chainned_cog.ChainableCogwheelBE;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.chainned_cog.packets.ServerboundChainableCogwheelRidingPacket;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class ChainableCogwheelRidingHandler {

    public static BlockPos ridingChainConveyor;
    public static float chainPosition;
    public static BlockPos ridingConnection;
    public static boolean flipped;
    public static int catchingUp;

    public static void embark(BlockPos lift, float position, BlockPos connection) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        ridingChainConveyor = lift;
        chainPosition = position;
        ridingConnection = connection;
        catchingUp = 20;
        if (mc.level.getBlockEntity(ridingChainConveyor) instanceof ChainableCogwheelBE ccbe) flipped = ccbe.getSpeed() < 0;

        Component component = Component.translatable("mount.onboard", mc.options.keyShift.getTranslatedKeyMessage());
        mc.gui.setOverlayMessage(component, false);
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.CHAIN_HIT, 1f, 0.5f));
    }

    public static void clientTick() {
        if (ridingChainConveyor == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (mc.isPaused()) return;
        if (!AllTags.AllItemTags.CHAIN_RIDEABLE.matches(mc.player.getMainHandItem())) {
            stopRiding();
            return;
        }
        BlockEntity blockEntity = mc.level.getBlockEntity(ridingChainConveyor);
        if (mc.player.isShiftKeyDown() || !(blockEntity instanceof ChainableCogwheelBE ccbe)) {
            stopRiding();
            return;
        }
        if (ridingConnection != null && !ccbe.connections.contains(ridingConnection)) {
            stopRiding();
            return;
        }

        ccbe.prepareStats();

        float chainYOffset = 0.5f * mc.player.getScale();
        Vec3 playerPosition = mc.player.position().add(0, mc.player.getBoundingBox().getYsize() + chainYOffset, 0);

        updateTargetPosition(mc, ccbe);

        blockEntity = mc.level.getBlockEntity(ridingChainConveyor);
        if (!(blockEntity instanceof ChainableCogwheelBE)) return;

        ccbe = (ChainableCogwheelBE) blockEntity;
        ccbe.prepareStats();

        Vec3 targetPosition;

        if (ridingConnection != null) {
            var stats = ccbe.connectionStats.get(ridingConnection);
            targetPosition = stats.start().add((stats.end().subtract(stats.start())).normalize().scale(Math.min(stats.chainLength(), chainPosition)));
        } else targetPosition = Vec3.atBottomCenterOf(ridingChainConveyor).add(VecHelper.rotate(new Vec3(0, 0.5, 1), chainPosition, Direction.Axis.Y));

        if (catchingUp > 0) catchingUp--;

        Vec3 diff = targetPosition.subtract(playerPosition);
        if (catchingUp == 0 && (diff.length() > 3 || diff.y < -1)) {
            stopRiding();
            return;
        }
        mc.player.setDeltaMovement(mc.player.getDeltaMovement().scale(0.5).add(diff.scale(0.25)));
        if (AnimationTickHolder.getTicks() % 10 == 0) CatnipServices.NETWORK.sendToServer(new ServerboundChainableCogwheelRidingPacket(ridingChainConveyor, false));
    }

    private static void stopRiding() {
        if (ridingChainConveyor != null) CatnipServices.NETWORK.sendToServer(new ServerboundChainableCogwheelRidingPacket(ridingChainConveyor, true));
        ridingChainConveyor = null;
        ridingConnection = null;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.CHAIN_HIT, 0.75f, 0.35f));
    }

    private static void updateTargetPosition(Minecraft mc, ChainableCogwheelBE ccbe) {
        if (mc.level == null || mc.player == null) return;
        float serverSpeed = ServerSpeedProvider.get();
        var beSpeed = ccbe.getSpeed() * 1.25f;
        float speed = beSpeed / 360f;
        float radius = 1f;
        float distancePerTick = Math.abs(speed);
        float degreesPerTick = (speed / (Mth.PI * radius)) * 360f;

        if (ridingConnection != null) {
            var stats = ccbe.connectionStats.get(ridingConnection);
            var length = stats.chainLength();


            if (flipped != beSpeed < 0) {
                flipped = beSpeed < 0;
                ridingChainConveyor = ccbe.getBlockPos().offset(ridingConnection);
                chainPosition = length - chainPosition;
                ridingConnection = ridingConnection.multiply(-1);
                return;
            }

            chainPosition += serverSpeed * distancePerTick;
            chainPosition = Math.min(length, chainPosition);
            if (chainPosition < length) return;

            // transfer to other
            if (mc.level.getBlockEntity(ccbe.getBlockPos().offset(ridingConnection)) instanceof ChainableCogwheelBE ccbe2) {
                chainPosition = ccbe.wrapAngle((beSpeed < 0 ? 0 : 180) + 270 + stats.tangentAngle());
                ridingChainConveyor = ccbe2.getBlockPos();
                ridingConnection = null;
            }
            return;
        }
        float prevChainPosition = chainPosition;
        chainPosition += serverSpeed * degreesPerTick;
        chainPosition = ccbe.wrapAngle(chainPosition);

        BlockPos nearestLooking = BlockPos.ZERO;
        double bestDiff = Double.MAX_VALUE;
        for (BlockPos connection : ccbe.connections) {
            double diff = Vec3.atLowerCornerOf(connection).normalize().distanceToSqr(mc.player.getLookAngle().normalize());
            if (diff > bestDiff) continue;
            nearestLooking = connection;
            bestDiff = diff;
        }
        if (nearestLooking == BlockPos.ZERO) return;

        float offBranchAngle = ccbe.connectionStats.get(nearestLooking).tangentAngle();
        if (!ccbe.loopThresholdCrossed(chainPosition, prevChainPosition, (beSpeed < 0 ? 0 : 180) + 270 + offBranchAngle)) return;

        chainPosition = 0;
        ridingConnection = nearestLooking;
    }
}
