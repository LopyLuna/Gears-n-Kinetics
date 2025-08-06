package dev.lopyluna.gnkinetics.events;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lopyluna.gnkinetics.Gears;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.chainned_cog.handlers.ChainableCogwheelConnectionHandler;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.chainned_cog.handlers.ChainableCogwheelInteractionHandler;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.chainned_cog.handlers.ChainableCogwheelRidingHandler;
import net.createmod.catnip.render.DefaultSuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@SuppressWarnings("removal")
@EventBusSubscriber(modid = Gears.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onTickPre(ClientTickEvent.Pre event) {
        onTick( true);
    }
    @SubscribeEvent
    public static void onTickPost(ClientTickEvent.Post event) {
        onTick(false);
    }

    public static void onTick(boolean isPreEvent) {
        if (!isGameActive()) return;
        var mc = Minecraft.getInstance();
        var level = mc.level;
        if (isPreEvent) return;

        ChainableCogwheelInteractionHandler.clientTick();
        ChainableCogwheelRidingHandler.clientTick();
        ChainableCogwheelConnectionHandler.clientTick();
    }


    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        var ms = event.getPoseStack();
        ms.pushPose();
        var buffer = DefaultSuperRenderTypeBuffer.getInstance();
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        ChainableCogwheelInteractionHandler.drawCustomBlockSelection(ms, buffer, camera);

        buffer.draw();
        RenderSystem.enableCull();
        ms.popPose();
    }

    protected static boolean isGameActive() {
        return !(Minecraft.getInstance().level == null || Minecraft.getInstance().player == null);
    }
}
