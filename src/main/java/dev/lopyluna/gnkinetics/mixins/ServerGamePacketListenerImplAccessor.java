package dev.lopyluna.gnkinetics.mixins;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerGamePacketListenerImpl.class)
public interface ServerGamePacketListenerImplAccessor {
    @Accessor("aboveGroundTickCount")
    void aboveGroundTickCount(int value);
    @Accessor("aboveGroundVehicleTickCount")
    void aboveGroundVehicleTickCount(int value);
}
