package dev.lopyluna.gnkinetics;

import dev.lopyluna.gnkinetics.register.client.GearsPartialModels;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import static dev.lopyluna.gnkinetics.Gears.MOD_ID;

@Mod(value = MOD_ID, dist = Dist.CLIENT)
public class GearsClient {

    public GearsClient(IEventBus modEventBus) {
        modEventBus.addListener(GearsClient::clientInit);
    }

    public static void clientInit(final FMLClientSetupEvent event) {
        GearsPartialModels.init();
    }
}
