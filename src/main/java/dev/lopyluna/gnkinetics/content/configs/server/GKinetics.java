package dev.lopyluna.gnkinetics.content.configs.server;

import dev.lopyluna.gnkinetics.content.configs.server.kinetics.GStress;
import net.createmod.catnip.config.ConfigBase;
import org.jetbrains.annotations.NotNull;

public class GKinetics extends ConfigBase {

    public final ConfigInt maxChainableCogwheelLength = i(32, 5, "maxChainableCogwheelLength",
            "Maximum length in blocks of chain conveyor connections.");
    public final ConfigInt maxChainableCogwheelConnections = i(6, 1, "maxChainableCogwheelConnections",
            "Maximum amount of connections each chain conveyor can have.");
    public final ConfigFloat cogCrankHungerMultiplier = f(.01f, 0, 1, "cogCrankHungerMultiplier",
            "multiplier used for calculating exhaustion from speed when a cog crank is turned.");
    public final GStress stressValues = nested(1, GStress::new, "Fine tune the kinetic stats of individual components");

    @Override
    public @NotNull String getName() {
        return "kinetics";
    }
}
