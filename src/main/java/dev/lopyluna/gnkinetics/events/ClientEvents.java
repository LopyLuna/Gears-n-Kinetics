package dev.lopyluna.gnkinetics.events;

import dev.lopyluna.gnkinetics.Gears;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@SuppressWarnings("removal")
@EventBusSubscriber(modid = Gears.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        Gears.LOGGER.info(Gears.FULL_NAME + " CLIENT SETUP");
    }
}
