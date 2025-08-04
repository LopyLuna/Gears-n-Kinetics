package dev.lopyluna.gnkinetics.content.configs;

import dev.lopyluna.gnkinetics.Gears;
import dev.lopyluna.gnkinetics.content.configs.server.GKinetics;
import net.createmod.catnip.config.ConfigBase;
import org.jetbrains.annotations.NotNull;

public class GServer extends ConfigBase {
    public final ConfigGroup server = group(0,
            "server", "Configs for the World");

    public final GKinetics kinetics = nested(0, GKinetics::new, "Parameters and abilities of "+ Gears.FULL_NAME +"'s kinetic mechanisms");

    @Override
    public @NotNull String getName() {
        return "server";
    }

    private static class Comments {
    }
}
