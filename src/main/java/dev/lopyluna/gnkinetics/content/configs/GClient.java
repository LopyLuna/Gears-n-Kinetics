package dev.lopyluna.gnkinetics.content.configs;

import net.createmod.catnip.config.ConfigBase;
import org.jetbrains.annotations.NotNull;

public class GClient extends ConfigBase {
    public final ConfigGroup client = group(0, "client", "Configs for the Client");

    public final ConfigBool debugRotPropLoc = b(false, "debugRotPropLoc", Comments.debugRotPropLoc);
    public final ConfigInt debugRotPropDelay = i((int) (2.5f * 20f), "debugRotPropDelay", Comments.debugRotPropDelay);

    @Override
    public @NotNull String getName() {
        return "client";
    }

    private static class Comments {
        static String debugRotPropLoc = "Debug Rotation Propagator Location";
        static String debugRotPropDelay = "Debug Rotation Propagator Delay";
    }
}
