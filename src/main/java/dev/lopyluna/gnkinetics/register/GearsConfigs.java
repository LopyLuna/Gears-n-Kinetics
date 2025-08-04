package dev.lopyluna.gnkinetics.register;

import com.simibubi.create.api.stress.BlockStressValues;
import dev.lopyluna.gnkinetics.content.configs.GClient;
import dev.lopyluna.gnkinetics.content.configs.GCommon;
import dev.lopyluna.gnkinetics.content.configs.GServer;
import net.createmod.catnip.config.ConfigBase;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("removal")
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class GearsConfigs {
    private static final Map<ModConfig.Type, ConfigBase> CONFIGS = new EnumMap<>(ModConfig.Type.class);

    private static GClient client;
    private static GCommon common;
    private static GServer server;

    public static GClient client() { return client; }
    public static GCommon common() { return common; }
    public static GServer server() { return server; }

    public static ConfigBase byType(ModConfig.Type type) {
        return CONFIGS.get(type);
    }

    private static <T extends ConfigBase> T register(Supplier<T> factory, ModConfig.Type side) {
        Pair<T, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(builder -> {
            T config = factory.get();
            config.registerAll(builder);
            return config;
        });

        T config = specPair.getLeft();
        config.specification = specPair.getRight();
        CONFIGS.put(side, config);
        return config;
    }

    public static void register(ModLoadingContext context, ModContainer container) {
        client = register(GClient::new, ModConfig.Type.CLIENT);
        common = register(GCommon::new, ModConfig.Type.COMMON);
        server = register(GServer::new, ModConfig.Type.SERVER);

        for (Map.Entry<ModConfig.Type, ConfigBase> pair : CONFIGS.entrySet()) container.registerConfig(pair.getKey(), pair.getValue().specification);

        var stress = server().kinetics.stressValues;
        BlockStressValues.IMPACTS.registerProvider(stress::getImpact);
        BlockStressValues.CAPACITIES.registerProvider(stress::getCapacity);
    }

    @SubscribeEvent
    public static void onLoad(ModConfigEvent.Loading event) {
        for (var config : CONFIGS.values()) if (config.specification == event.getConfig().getSpec()) config.onLoad();
    }

    @SubscribeEvent
    public static void onReload(ModConfigEvent.Reloading event) {
        for (var config : CONFIGS.values()) if (config.specification == event.getConfig().getSpec()) config.onReload();
    }

}
