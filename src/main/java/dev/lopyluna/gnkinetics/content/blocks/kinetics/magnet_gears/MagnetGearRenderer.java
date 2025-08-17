package dev.lopyluna.gnkinetics.content.blocks.kinetics.magnet_gears;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;
import dev.lopyluna.gnkinetics.mixins.RotationPropagatorAccessor;
import dev.lopyluna.gnkinetics.register.client.GearsPartialModels;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import static dev.lopyluna.gnkinetics.content.blocks.kinetics.custom_cogs.CustomCogWheelRenderer.getAngleForLargeCogShaft;

public class MagnetGearRenderer extends KineticBlockEntityRenderer<SimpleKineticBlockEntity> {
    private final boolean large;
    public static MagnetGearRenderer small(BlockEntityRendererProvider.Context context) {
        return new MagnetGearRenderer(context, false);
    }
    public static MagnetGearRenderer large(BlockEntityRendererProvider.Context context) {
        return new MagnetGearRenderer(context, true);
    }
    public MagnetGearRenderer(BlockEntityRendererProvider.Context context, boolean large) {
        super(context);
        this.large = large;
    }

    @Override
    protected void renderSafe(SimpleKineticBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
        var level = be.getLevel();
        var blockState = be.getBlockState();
        if (!(blockState.getBlock() instanceof IRotate rot)) return;

        var axis = getRotationAxisOf(be);
        var pos = be.getBlockPos();

        for (var d : Iterate.directionsInAxis(getRotationAxisOf(be))) {
            if (!rot.hasShaftTowards(level, pos, blockState, d)) continue;

            var modifier = RotationPropagatorAccessor.getAxisModifier(be, d);
            var angle = getAngleForLargeCogShaft(be, axis, 0, modifier, modifier >= 0);
            var shaft = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, blockState, d);
            kineticRotationTransform(shaft, be, axis, angle, light);
            shaft.renderInto(ms, buffer.getBuffer(RenderType.solid()));
        }
    }

    @Override
    protected SuperByteBuffer getRotatedModel(SimpleKineticBlockEntity be, BlockState state) {
        return CachedBuffers.partialFacingVertical(large ? GearsPartialModels.SHAFTLESS_LARGE_MAGNET_GEAR : GearsPartialModels.SHAFTLESS_MAGNET_GEAR, state,
                Direction.fromAxisAndDirection(state.getValue(MagnetGearBlock.AXIS), Direction.AxisDirection.POSITIVE));
    }
}
