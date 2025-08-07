package dev.lopyluna.gnkinetics.content.configs.server;

import dev.lopyluna.gnkinetics.content.configs.server.kinetics.GStress;
import net.createmod.catnip.config.ConfigBase;
import org.jetbrains.annotations.NotNull;

public class GKinetics extends ConfigBase {

    public final ConfigInt maxChainableCogwheelLength = i(32, 5, "maxChainableCogwheelLength", "Maximum length in blocks of chain conveyor connections.");
    public final ConfigInt maxChainableCogwheelConnections = i(6, 1, "maxChainableCogwheelConnections", "Maximum amount of connections each chain conveyor can have.");
    public final GStress stressValues = nested(1, GStress::new, "Fine tune the kinetic stats of individual components");

    @Override
    public @NotNull String getName() {
        return "kinetics";
    }
}
