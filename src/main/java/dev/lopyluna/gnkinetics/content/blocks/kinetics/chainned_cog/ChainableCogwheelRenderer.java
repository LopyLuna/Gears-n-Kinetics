package dev.lopyluna.gnkinetics.content.blocks.kinetics.chainned_cog;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity.ConnectionStats;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import dev.lopyluna.gnkinetics.register.client.GearsPartialModels;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorRenderer.renderChain;

public class ChainableCogwheelRenderer extends KineticBlockEntityRenderer<ChainableCogwheelBE> {
    public ChainableCogwheelRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    public static final int MIP_DISTANCE = 48;

    @Override
    protected void renderSafe(ChainableCogwheelBE be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        var state = be.getBlockState();
        var axis = state.getValue(ChainableCogwheelBlock.AXIS);
        var facing = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);

        renderRotatingBuffer(be, CachedBuffers.partialFacingVertical(AllPartialModels.COGWHEEL_SHAFT, state, facing), ms, buffer.getBuffer(RenderType.cutoutMipped()), light);
        renderRotatingBuffer(be, CachedBuffers.partialFacingVertical(GearsPartialModels.CHAINABLE_COGWHEEL_CHAINS, state, facing), ms, buffer.getBuffer(RenderType.cutoutMipped()), light);
        renderRotatingBuffer(be, CachedBuffers.partialFacingVertical(GearsPartialModels.CHAINABLE_COGWHEEL, state, facing), ms, buffer.getBuffer(RenderType.cutoutMipped()), light);

        renderChains(be, axis, ms, buffer, light, overlay);
    }

    private void renderChains(ChainableCogwheelBE be, Direction.Axis axis, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        float time = AnimationTickHolder.getRenderTime(be.getLevel()) / (360f / Math.abs(be.getSpeed() * 1.25f));
        time %= 1;
        if (time < 0) time += 1;

        float animation = time - 0.5f;
        for (var dirNor : be.connections) {
            var level = be.getLevel();
            if (level == null) continue;
            ConnectionStats stats = be.connectionStats.get(dirNor);
            if (stats == null) continue;
            var startPos = be.getBlockPos();
            var endPos = startPos.offset(dirNor);
            var length = stats.chainLength();

            var end = endPos.getCenter();
            var start = startPos.getCenter();
            Vec3 diff = end.subtract(start);
            Vec3 dirVec = diff.normalize();
            Quaternionf q = new Quaternionf().rotationTo(new Vector3f(0, 1, 0), new Vector3f((float) dirVec.x, (float) dirVec.y, (float) dirVec.z));

            Vec3 offDir = VecHelper.rotate(dirVec, 90, axis).scale(12f/16f);
            Vec3 offset = dirVec.scale(0.5);

            ms.pushPose();
            var chain = TransformStack.of(ms);

            chain.center();

            chain.translate((float) offDir.x, (float) offDir.y, (float) offDir.z);
            chain.translate((float) offset.x, (float) offset.y, (float) offset.z);
            chain.rotate(q);

            chain.uncenter();

            int light1 = LightTexture.pack(level.getBrightness(LightLayer.BLOCK, startPos), level.getBrightness(LightLayer.SKY, startPos));
            int light2 = LightTexture.pack(level.getBrightness(LightLayer.BLOCK, startPos.offset(dirNor)), level.getBrightness(LightLayer.SKY, startPos.offset(dirNor)));
            boolean far = Minecraft.getInstance().level == be.getLevel() && !Minecraft.getInstance()
                    .getBlockEntityRenderDispatcher().camera.getPosition()
                    .closerThan(Vec3.atCenterOf(startPos).add(dirNor.getX() / 2f, dirNor.getY() / 2f, dirNor.getZ() / 2f), MIP_DISTANCE);

            renderChain(ms, buffer, 0 > be.getSpeed() ? animation : -animation, length, light1, light2, far);
            ms.popPose();
        }
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull ChainableCogwheelBE be) {
        return true;
    }

    @Override
    protected RenderType getRenderType(ChainableCogwheelBE be, BlockState state) {
        return RenderType.cutoutMipped();
    }
}
