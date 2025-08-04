package dev.lopyluna.gnkinetics.content.client;

import dev.lopyluna.gnkinetics.register.GearsConfigs;
import net.createmod.catnip.outliner.Outline;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.theme.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;
import java.util.function.Consumer;

public class DebugOutliner extends Outliner {
    private static final DebugOutliner instance = new DebugOutliner();

    public static DebugOutliner getDebugInstance() {
        return instance;
    }





    public Outline.OutlineParams showBlockPosInstance(Object slot, BlockPos pos) {
        return showAABB(slot, new AABB(pos)).colored(Color.WHITE).lineWidth(1 / 16f);
    }

    public Outline.OutlineParams showBlockPosInstance(BlockPos pos) {
        return showBlockPosInstance(pos, pos);
    }

    public void showBlockPositionsInstance(Iterable<BlockPos> positions, int color) {
        for (var pos : positions) showBlockPosInstance(pos).colored(color);
    }

    public void showBlockPositionsInstance(Iterable<BlockPos> positions, Consumer<Outline.OutlineParams> paramsApplier) {
        for (var pos : positions) paramsApplier.accept(showBlockPosInstance(pos));
    }



    public Outline.OutlineParams showBlockPosInstance(Object slot, BlockPos pos, int ttl) {
        return showAABB(slot, new AABB(pos), ttl).colored(Color.WHITE).lineWidth(1 / 16f);
    }

    public Outline.OutlineParams showBlockPosInstance(BlockPos pos, int ttl) {
        return showBlockPosInstance(pos, pos, ttl);
    }

    public void showBlockPositionsInstance(Iterable<BlockPos> positions, int color, int ttl) {
        for (var pos : positions) showBlockPosInstance(pos, ttl).colored(color);
    }

    public void showBlockPositionsInstance(Iterable<BlockPos> positions, Consumer<Outline.OutlineParams> paramsApplier, int ttl) {
        for (var pos : positions) paramsApplier.accept(showBlockPosInstance(pos, ttl));
    }





    public static void showBlockPositions(List<BlockPos> positions, int color) {
        CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> showBlockPositionsClient(positions, color));
    }
    public static void showBlockPositions(List<BlockPos> positions, Consumer<Outline.OutlineParams> paramsApplier) {
        CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> showBlockPositionsClient(positions, paramsApplier));
    }
    public static void showBlockPos(Object slot, BlockPos pos) {
        CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> showBlockPosClient(slot, pos));
    }
    public static void showBlockPos(BlockPos pos) {
        CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> showBlockPosClient(pos));
    }
    public static void showBlockPos(Object slot, BlockPos pos, Consumer<Outline.OutlineParams> paramsApplier) {
        CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> showBlockPosClient(slot, pos, paramsApplier));
    }
    public static void showBlockPos(BlockPos pos, Consumer<Outline.OutlineParams> paramsApplier) {
        CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> showBlockPosClient(pos, paramsApplier));
    }



    public static void showBlockPositions(List<BlockPos> positions, int color, int ttl) {
        CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> showBlockPositionsClient(positions, color, ttl));
    }
    public static void showBlockPositions(List<BlockPos> positions, Consumer<Outline.OutlineParams> paramsApplier, int ttl) {
        CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> showBlockPositionsClient(positions, paramsApplier, ttl));
    }
    public static void showBlockPos(Object slot, BlockPos pos, int ttl) {
        CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> showBlockPosClient(slot, pos, ttl));
    }
    public static void showBlockPos(BlockPos pos, int ttl) {
        CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> showBlockPosClient(pos, ttl));
    }
    public static void showBlockPos(Object slot, BlockPos pos, Consumer<Outline.OutlineParams> paramsApplier, int ttl) {
        CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> showBlockPosClient(slot, pos, paramsApplier, ttl));
    }
    public static void showBlockPos(BlockPos pos, Consumer<Outline.OutlineParams> paramsApplier, int ttl) {
        CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> showBlockPosClient(pos, paramsApplier, ttl));
    }

    public static void showBlockPositionsTTL(List<BlockPos> positions, Consumer<Outline.OutlineParams> paramsApplier) {
        CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> showBlockPositionsClientTTL(positions, paramsApplier));
    }




    @OnlyIn(Dist.CLIENT)
    protected static void showBlockPosClient(Object slot, BlockPos pos, Consumer<Outline.OutlineParams> paramsApplier) {
        if (GearsConfigs.client().debugRotPropLoc.get()) paramsApplier.accept(getDebugInstance().showBlockPosInstance(slot, pos));
    }
    @OnlyIn(Dist.CLIENT)
    protected static void showBlockPosClient(BlockPos pos, Consumer<Outline.OutlineParams> paramsApplier) {
        if (GearsConfigs.client().debugRotPropLoc.get()) paramsApplier.accept(getDebugInstance().showBlockPosInstance(pos));
    }
    @OnlyIn(Dist.CLIENT)
    protected static void showBlockPosClient(Object slot, BlockPos pos) {
        if (GearsConfigs.client().debugRotPropLoc.get()) getDebugInstance().showBlockPosInstance(slot, pos);
    }
    @OnlyIn(Dist.CLIENT)
    protected static void showBlockPosClient(BlockPos pos) {
        if (GearsConfigs.client().debugRotPropLoc.get()) getDebugInstance().showBlockPosInstance(pos);
    }
    @OnlyIn(Dist.CLIENT)
    protected static void showBlockPositionsClient(List<BlockPos> positions, int color) {
        if (GearsConfigs.client().debugRotPropLoc.get()) getDebugInstance().showBlockPositionsInstance(positions, color);
    }
    @OnlyIn(Dist.CLIENT)
    protected static void showBlockPositionsClient(List<BlockPos> positions, Consumer<Outline.OutlineParams> paramsApplier) {
        if (GearsConfigs.client().debugRotPropLoc.get()) getDebugInstance().showBlockPositionsInstance(positions, paramsApplier);
    }


    @OnlyIn(Dist.CLIENT)
    protected static void showBlockPositionsClientTTL(List<BlockPos> positions, Consumer<Outline.OutlineParams> paramsApplier) {
        if (GearsConfigs.client().debugRotPropLoc.get()) getDebugInstance().showBlockPositionsInstance(positions, paramsApplier, GearsConfigs.client().debugRotPropDelay.get());
    }

    @OnlyIn(Dist.CLIENT)
    protected static void showBlockPosClient(Object slot, BlockPos pos, Consumer<Outline.OutlineParams> paramsApplier, int ttl) {
        if (GearsConfigs.client().debugRotPropLoc.get()) paramsApplier.accept(getDebugInstance().showBlockPosInstance(slot, pos, ttl));
    }
    @OnlyIn(Dist.CLIENT)
    protected static void showBlockPosClient(BlockPos pos, Consumer<Outline.OutlineParams> paramsApplier, int ttl) {
        if (GearsConfigs.client().debugRotPropLoc.get()) paramsApplier.accept(getDebugInstance().showBlockPosInstance(pos, ttl));
    }
    @OnlyIn(Dist.CLIENT)
    protected static void showBlockPosClient(Object slot, BlockPos pos, int ttl) {
        if (GearsConfigs.client().debugRotPropLoc.get()) getDebugInstance().showBlockPosInstance(slot, pos, ttl);
    }
    @OnlyIn(Dist.CLIENT)
    protected static void showBlockPosClient(BlockPos pos, int ttl) {
        if (GearsConfigs.client().debugRotPropLoc.get()) getDebugInstance().showBlockPosInstance(pos, ttl);
    }
    @OnlyIn(Dist.CLIENT)
    protected static void showBlockPositionsClient(List<BlockPos> positions, int color, int ttl) {
        if (GearsConfigs.client().debugRotPropLoc.get()) getDebugInstance().showBlockPositionsInstance(positions, color, ttl);
    }
    @OnlyIn(Dist.CLIENT)
    protected static void showBlockPositionsClient(List<BlockPos> positions, Consumer<Outline.OutlineParams> paramsApplier, int ttl) {
        if (GearsConfigs.client().debugRotPropLoc.get()) getDebugInstance().showBlockPositionsInstance(positions, paramsApplier, ttl);
    }
}
