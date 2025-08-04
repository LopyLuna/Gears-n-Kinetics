package dev.lopyluna.gnkinetics.register;

import dev.lopyluna.gnkinetics.Gears;
import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.CatnipPacketRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Locale;

public enum GearsPackets implements BasePacketPayload.PacketTypeProvider {
    ;

    private final CatnipPacketRegistry.PacketType<?> type;

    <T extends BasePacketPayload> GearsPackets(Class<T> clazz, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        var name = name().toLowerCase(Locale.ROOT);
        type = new CatnipPacketRegistry.PacketType<>(new CustomPacketPayload.Type<>(Gears.loc(name)), clazz, codec);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends CustomPacketPayload> CustomPacketPayload.Type<T> getType() {
        return (CustomPacketPayload.Type<T>) this.type.type();
    }

    public static void register() {
        var packetRegistry = new CatnipPacketRegistry(Gears.MOD_ID, 1);
        for (var packet : values()) packetRegistry.registerPacket(packet.type);
        packetRegistry.registerAllPackets();
    }
}
