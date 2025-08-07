package dev.lopyluna.gnkinetics.content.blocks.kinetics.chainned_cog.packets;

import com.simibubi.create.content.kinetics.chainConveyor.ServerChainConveyorHandler;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.chainned_cog.ChainableCogwheelBE;
import dev.lopyluna.gnkinetics.mixins.ServerGamePacketListenerImplAccessor;
import dev.lopyluna.gnkinetics.register.GearsConfigs;
import dev.lopyluna.gnkinetics.register.GearsPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public class ServerboundChainableCogwheelRidingPacket extends BlockEntityConfigurationPacket<ChainableCogwheelBE> {
	public static final StreamCodec<ByteBuf, ServerboundChainableCogwheelRidingPacket> STREAM_CODEC = StreamCodec.composite(
	    BlockPos.STREAM_CODEC, packet -> packet.pos,
		ByteBufCodecs.BOOL, packet -> packet.stop,
	    ServerboundChainableCogwheelRidingPacket::new
	);

	private final boolean stop;

	public ServerboundChainableCogwheelRidingPacket(BlockPos pos, boolean stop) {
		super(pos);
		this.stop = stop;
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return GearsPackets.CHAIN_COGWHEEL_RIDING;
	}

	@Override
	protected int maxRange() {
		return GearsConfigs.server().kinetics.maxChainableCogwheelLength.get() * 2;
	}

	@Override
	protected void applySettings(ServerPlayer sender, ChainableCogwheelBE be) {
		sender.fallDistance = 0;
		((ServerGamePacketListenerImplAccessor) sender.connection).aboveGroundTickCount(0);
		((ServerGamePacketListenerImplAccessor) sender.connection).aboveGroundVehicleTickCount(0);

		if (stop) ServerChainConveyorHandler.handleStopRidingPacket(sender);
		else ServerChainConveyorHandler.handleTTLPacket(sender);
	}
}
