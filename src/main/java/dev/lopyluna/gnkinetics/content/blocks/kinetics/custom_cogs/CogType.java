package dev.lopyluna.gnkinetics.content.blocks.kinetics.custom_cogs;

public enum CogType {
    SOLID(true),
    HOLLOWED(false),
    SHAFTLESS(false);
    public final boolean hasShaft;
    CogType(boolean hasShaft) {
        this.hasShaft = hasShaft;
    }
}
