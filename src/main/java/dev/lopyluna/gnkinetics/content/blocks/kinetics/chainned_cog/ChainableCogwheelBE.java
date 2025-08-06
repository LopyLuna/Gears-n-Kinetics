package dev.lopyluna.gnkinetics.content.blocks.kinetics.chainned_cog;

import com.simibubi.create.api.contraption.transformable.TransformableBlockEntity;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity.ConnectionStats;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorShape;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.chainned_cog.handlers.ChainableCogwheelInteractionHandler;
import net.createmod.catnip.codecs.CatnipCodecUtils;
import net.createmod.catnip.codecs.CatnipCodecs;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.function.Consumer;

import static com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity.getChainCost;

public class ChainableCogwheelBE extends KineticBlockEntity implements TransformableBlockEntity {
    public Set<BlockPos> connections = new HashSet<>();
    public Map<BlockPos, ConnectionStats> connectionStats;

    public boolean reversed;
    public boolean cancelDrops;
    public boolean checkInvalid;

    BlockPos chainDestroyedEffectToSend;

    public ChainableCogwheelBE(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        checkInvalid = true;
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        updateChainShapes();
    }

    @Override
    public void tick() {
        super.tick();
        assert level != null;
        if (checkInvalid && !level.isClientSide()) {
            checkInvalid = false;
            removeInvalidConnections();
        }

        var reversedPreviously = reversed;
        prepareStats();
        if (reversedPreviously != reversed) notifyUpdate();
    }

    public void removeInvalidConnections() {
        assert level != null;
        var changed = false;
        for (var iterator = connections.iterator(); iterator.hasNext(); ) {
            var nextPos = iterator.next();
            var targetPos = worldPosition.offset(nextPos);
            if (!level.isLoaded(targetPos)) continue;
            if (level.getBlockEntity(targetPos) instanceof ChainableCogwheelBE ccbe && ccbe.connections.contains(nextPos.multiply(-1))) continue;
            iterator.remove();
            changed = true;
        }
        if (changed) notifyUpdate();
    }

    //Contraption#moveBlock

    public void notifyConnectedToValidate() {
        assert level != null;
        for (var blockPos : connections) {
            var target = worldPosition.offset(blockPos);
            if (!level.isLoaded(target)) continue;
            if (level.getBlockEntity(target) instanceof ChainableCogwheelBE ccbe) ccbe.checkInvalid = true;
        }
    }

    public boolean loopThresholdCrossed(float chainPosition, float prevChainPosition, float offBranchAngle) {
        int sign1 = Mth.sign(AngleHelper.getShortestAngleDiff(offBranchAngle, prevChainPosition));
        int sign2 = Mth.sign(AngleHelper.getShortestAngleDiff(offBranchAngle, chainPosition));
        boolean notCrossed = sign1 >= sign2 && !reversed || sign1 <= sign2 && reversed;
        return !notCrossed;
    }

    @Override
    public void notifyUpdate() {
        assert level != null;
        level.blockEntityChanged(worldPosition);
        sendData();
    }

    public void prepareStats() {
        float speed = getSpeed();
        if (reversed != speed < 0 && speed != 0) {
            reversed = speed < 0;
            connectionStats = null;
        }
        if (connectionStats == null) {
            connectionStats = new HashMap<>();
            connections.forEach(this::calculateConnectionStats);
        }
    }

    private void calculateConnectionStats(BlockPos connection) {
        boolean reversed = getSpeed() < 0;
        float offBranchDistance = 180f;
        float direction = Mth.RAD_TO_DEG * (float) Mth.atan2(connection.getX(), connection.getZ());
        float angle = wrapAngle(direction - offBranchDistance * (reversed ? -1 : 1));

        var end = worldPosition.offset(connection).getCenter();
        var start = worldPosition.getCenter();


        var axis = getBlockState().getValue(ChainableCogwheelBlock.AXIS);
        Vec3 diff = end.subtract(start);
        Vec3 dirVec = diff.normalize();
        Vec3 offDir = VecHelper.rotate(dirVec, reversed ? 90 : -90, axis).scale(12f/16f);
        end = end.add((float) offDir.x, (float) offDir.y, (float) offDir.z);
        start = start.add((float) offDir.x, (float) offDir.y, (float) offDir.z);

        float length = (float) start.distanceTo(end);
        connectionStats.put(connection, new ConnectionStats(angle, length, start, end));
    }

    //PACKET
    public boolean cantAddConnectionTo(BlockPos target) {
        var localTargetPos = target.subtract(worldPosition);
        boolean added = connections.add(localTargetPos);
        if (added) {
            notifyUpdate();
            calculateConnectionStats(localTargetPos);
            updateChainShapes();
        }

        detachKinetics();
        updateSpeed = true;
        return !added;
    }

    public void chainDestroyed(BlockPos target, boolean spawnDrops, boolean sendEffect) {
        assert level != null;

        int chainCount = getChainCost(target);
        if (sendEffect) {
            chainDestroyedEffectToSend = target;
            sendData();
        }
        if (!spawnDrops) return;
        if (!forPointsAlongChains(target, chainCount, vec -> level.addFreshEntity(new ItemEntity(level, vec.x, vec.y, vec.z, new ItemStack(Items.CHAIN))))) while (chainCount > 0) {
            Block.popResource(level, worldPosition, new ItemStack(Blocks.CHAIN.asItem(), Math.min(chainCount, 64)));
            chainCount -= 64;
        }
    }

    public void removeConnectionTo(BlockPos target) {
        var localTarget = target.subtract(worldPosition);
        if (!connections.contains(localTarget)) return;

        detachKinetics();
        connections.remove(localTarget);
        connectionStats.remove(localTarget);
        notifyUpdate();
        updateChainShapes();
        updateSpeed = true;
    }

    private void updateChainShapes() {
        prepareStats();

        List<ChainConveyorShape> shapes = new ArrayList<>();
        shapes.add(new ChainableCogwheelShapeBB(Vec3.atBottomCenterOf(BlockPos.ZERO)));
        for (BlockPos target : connections) {
            ConnectionStats stats = connectionStats.get(target);
            if (stats == null) continue;
            Vec3 localStart = stats.start().subtract(Vec3.atLowerCornerOf(worldPosition));
            Vec3 localEnd = stats.end().subtract(Vec3.atLowerCornerOf(worldPosition));

            shapes.add(new ChainConveyorShape.ChainConveyorOBB(target, localStart, localEnd));
        }

        if (level != null && level.isClientSide())
            ChainableCogwheelInteractionHandler.loadedChains.get(level).put(worldPosition, shapes);
    }

    @Override
    public void remove() {
        super.remove();
        if (level == null || !level.isClientSide()) return;
        for (var blockPos : connections) spawnDestroyParticles(blockPos);
    }

    private void spawnDestroyParticles(BlockPos blockPos) {
        assert level != null;
        forPointsAlongChains(blockPos, (int) Math.round(Vec3.atLowerCornerOf(blockPos).length() * 8),
                vec -> level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.CHAIN.defaultBlockState()),
                        vec.x, vec.y, vec.z, 0, 0, 0));
    }

    @Override
    public void destroy() {
        super.destroy();
        assert level != null;

        for (var blockPos : connections) {
            chainDestroyed(blockPos, !cancelDrops, false);
            if (level.getBlockEntity(worldPosition.offset(blockPos)) instanceof ChainableCogwheelBE ccbe) ccbe.removeConnectionTo(worldPosition);
        }
    }

    public boolean forPointsAlongChains(BlockPos connection, int positions, Consumer<Vec3> callback) {
        prepareStats();
        var stats = connectionStats.get(connection);
        if (stats == null) return false;
        Vec3 start = stats.start();
        Vec3 direction = stats.end().subtract(start);
        Vec3 origin = Vec3.atCenterOf(worldPosition);
        Vec3 normal = direction.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 offset = start.subtract(origin);
        Vec3 start2 = origin.add(offset.add(normal.scale(-2 * normal.dot(offset))));

        for (boolean firstChain : Iterate.trueAndFalse) {
            int steps = positions / 2;
            if (firstChain) steps += positions % 2;
            for (int i = 0; i < steps; i++) callback.accept((firstChain ? start : start2).add(direction.scale((0.5 + i) / steps)));
        }
        return true;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (level != null && level.isClientSide()) ChainableCogwheelInteractionHandler.loadedChains.get(level).invalidate(worldPosition);
    }

    @Override
    public List<BlockPos> addPropagationLocations(IRotate block, BlockState state, List<BlockPos> neighbours) {
        for (var offset : BlockPos.betweenClosed(-1, -1, -1, 1, 1, 1)) if (offset.distSqr(BlockPos.ZERO) == 2) neighbours.add(worldPosition.offset(offset));
        connections.forEach(p -> neighbours.add(worldPosition.offset(p)));
        return super.addPropagationLocations(block, state, neighbours);
    }

    @Override
    protected boolean canPropagateDiagonally(IRotate block, BlockState state) {
        return state.getBlock() instanceof ICogWheel || block instanceof ICogWheel;
    }

    @Override
    public float propagateRotationTo(KineticBlockEntity target, BlockState stateFrom, BlockState stateTo, BlockPos diff, boolean connectedViaAxes, boolean connectedViaCogs) {
        if (connections.contains(target.getBlockPos().subtract(worldPosition))) {
            if (!(target instanceof ChainableCogwheelBE)) return 0;
            return 1;
        }
        return super.propagateRotationTo(target, stateFrom, stateTo, diff, connectedViaAxes, connectedViaCogs);
    }

    @Override
    public void writeSafe(CompoundTag tag, HolderLookup.Provider registries) {
        super.writeSafe(tag, registries);
        tag.put("Connections", CatnipCodecUtils.encode(CatnipCodecs.set(BlockPos.CODEC), registries, connections).orElseThrow());
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        if (clientPacket && chainDestroyedEffectToSend != null) {
            compound.put("DestroyEffect", NbtUtils.writeBlockPos(chainDestroyedEffectToSend));
            chainDestroyedEffectToSend = null;
        }
        compound.put("Connections", CatnipCodecUtils.encode(CatnipCodecs.set(BlockPos.CODEC), registries, connections).orElseThrow());
    }


    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        if (clientPacket && compound.contains("DestroyEffect") && level != null) spawnDestroyParticles(NBTHelper.readBlockPos(compound, "DestroyEffect"));

        int sizeBefore = connections.size();
        connections.clear();
        CatnipCodecUtils.decode(CatnipCodecs.set(BlockPos.CODEC), registries, compound.get("Connections")).ifPresent(connections::addAll);

        connectionStats = null;
        updateChainShapes();

        if (connections.size() != sizeBefore && level != null && level.isClientSide) invalidateRenderBoundingBox();
    }

    public float wrapAngle(float angle) {
        angle %= 360;
        if (angle < 0) angle += 360;
        return angle;
    }


    @Override
    public void transform(BlockEntity blockEntity, StructureTransform transform) {
        if (connections == null || connections.isEmpty()) return;
        connections = new HashSet<>(connections.stream().map(transform::applyWithoutOffset).toList());
        connectionStats = null;
        notifyUpdate();
    }
}
