package dev.lopyluna.gnkinetics.content.blocks.kinetics.chainned_cog.packets;

import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.chainned_cog.ChainableCogwheelBE;
import dev.lopyluna.gnkinetics.register.GearsConfigs;
import dev.lopyluna.gnkinetics.register.GearsPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity.getChainCost;
import static com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity.getChainsFromInventory;

public class ChainableCogwheelConnectionPacket extends BlockEntityConfigurationPacket<ChainableCogwheelBE> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ChainableCogwheelConnectionPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, packet -> packet.pos,
            BlockPos.STREAM_CODEC, packet -> packet.targetPos,
            ItemStack.STREAM_CODEC, packet -> packet.chain,
            ByteBufCodecs.BOOL, packet -> packet.connect,
            ChainableCogwheelConnectionPacket::new
    );

    private final BlockPos targetPos;
    private final ItemStack chain;
    private final boolean connect;

    public ChainableCogwheelConnectionPacket(BlockPos pos, BlockPos targetPos, ItemStack chain, boolean connect) {
        super(pos);
        this.targetPos = targetPos;
        this.chain = chain;
        this.connect = connect;
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return GearsPackets.CHAIN_COGWHEEL_CONNECT;
    }

    @Override
    protected int maxRange() {
        return GearsConfigs.server().kinetics.maxChainableCogwheelLength.get() + 16;
    }

    @Override
    protected void applySettings(ServerPlayer player, ChainableCogwheelBE be) {
        var level = be.getLevel();
        var pos = be.getBlockPos();
        if (level == null) return;
        if (!pos.closerThan(targetPos, maxRange() - 16 + 1)) return;
        if (!(level.getBlockEntity(targetPos) instanceof ChainableCogwheelBE clbe)) return;

        if (connect && !player.isCreative()) {
            int chainCost = getChainCost(targetPos.subtract(pos));
            boolean hasEnough = getChainsFromInventory(player, chain, chainCost, true);
            if (!hasEnough) return;
            getChainsFromInventory(player, chain, chainCost, false);
        }

        if (!connect) {
            if (!player.isCreative()) {
                int chainCost = getChainCost(targetPos.subtract(pos));
                while (chainCost > 0) {
                    player.getInventory().placeItemBackInInventory(new ItemStack(Items.CHAIN, Math.min(chainCost, 64)));
                    chainCost -= 64;
                }
            }
            be.chainDestroyed(targetPos.subtract(pos), false, true);
            level.playSound(null, player.blockPosition(), SoundEvents.CHAIN_BREAK, SoundSource.BLOCKS);
        }

        if (connect) {
            if (clbe.cantAddConnectionTo(pos)) return;
        } else clbe.removeConnectionTo(pos);

        if (connect) {
            if (be.cantAddConnectionTo(targetPos)) clbe.removeConnectionTo(pos);
        } else be.removeConnectionTo(targetPos);
    }
}
