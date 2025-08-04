package dev.lopyluna.gnkinetics.events;

import dev.lopyluna.gnkinetics.Gears;
import dev.lopyluna.gnkinetics.content.utils.GearsRemapper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@SuppressWarnings("removal")
@EventBusSubscriber(modid = Gears.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class CommonEvents {
    public static void commonSetup(final FMLCommonSetupEvent event) {
        GearsRemapper.register();
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        Gears.LOGGER.info(Gears.NAME + " CAPABILITIES SETUP");
    }
}
