package dev.lopyluna.gnkinetics.events;

import dev.lopyluna.gnkinetics.content.blocks.kinetics.chainned_cog.handlers.ChainableCogwheelConnectionHandler;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.chainned_cog.handlers.ChainableCogwheelInteractionHandler;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(Dist.CLIENT)
public class InputEvents {


    @SubscribeEvent
    public static void onClickInput(InputEvent.InteractionKeyMappingTriggered event) {
        var mc = Minecraft.getInstance();
        if (mc.screen != null) return;
        var key = event.getKeyMapping();

        if (key == mc.options.keyUse && ChainableCogwheelConnectionHandler.onRightClick()) {
            event.setCanceled(true);
            return;
        }

        if (!event.isUseItem()) return;

        if (ChainableCogwheelInteractionHandler.onUse()) event.setCanceled(true);
    }
}
