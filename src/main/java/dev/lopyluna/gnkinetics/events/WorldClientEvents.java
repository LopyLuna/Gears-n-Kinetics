package dev.lopyluna.gnkinetics.events;

import dev.lopyluna.gnkinetics.Gears;
import dev.lopyluna.gnkinetics.content.client.DebugOutliner;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.DefaultSuperRenderTypeBuffer;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import static net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS;

@SuppressWarnings("removal")
@EventBusSubscriber(modid = Gears.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class WorldClientEvents {
    private static final Minecraft mc = Minecraft.getInstance();

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (mc.getConnection() == null || mc.player == null || mc.level == null) return;
        DebugOutliner.getDebugInstance().tickOutlines();
    }

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (mc.getConnection() == null || mc.player == null || mc.level == null || event.getStage() != AFTER_TRIPWIRE_BLOCKS) return;
        var ms = event.getPoseStack();
        var cameraPos = event.getCamera().getPosition();
        var partialTicks = AnimationTickHolder.getPartialTicks();

        ms.pushPose();
        SuperRenderTypeBuffer buffer = DefaultSuperRenderTypeBuffer.getInstance();
        DebugOutliner.getDebugInstance().renderOutlines(ms, buffer, cameraPos, partialTicks);
        buffer.draw();
        ms.pushPose();
    }
}
