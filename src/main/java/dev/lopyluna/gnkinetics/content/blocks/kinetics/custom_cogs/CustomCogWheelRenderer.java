package dev.lopyluna.gnkinetics.content.blocks.kinetics.custom_cogs;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.DirectionalShaftHalvesBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntityRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.INewAxisConnection;
import dev.lopyluna.gnkinetics.mixins.RotationPropagatorAccessor;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class CustomCogWheelRenderer extends BracketedKineticBlockEntityRenderer {
    protected boolean hasShaft;

    public CustomCogWheelRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @SuppressWarnings("all")
    @Override
    protected void renderSafe(BracketedKineticBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        var state = be.getBlockState();
        if (!(state.getBlock() instanceof CustomCogWheelBlock cog)) return;
        var level = be.getLevel();
        if (level == null) return;
        //if (VisualizationManager.supportsVisualization(level)) return;
        var pos = be.getBlockPos();

        hasShaft = cog.cogType.hasShaft;
        var isHollowed = cog.hasShaftHollowed(state);

        var axis = getRotationAxisOf(be);
        var facing = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
        PartialModel model = cog.model;

        renderRotatingBuffer(be, CachedBuffers.partialFacingVertical(model, state, facing), ms, buffer.getBuffer(RenderType.cutoutMipped()), light);

        if (hasShaft) {
            var angle = getAngleForLargeCogShaft(be, axis);
            var shaft = CachedBuffers.partialFacingVertical(AllPartialModels.COGWHEEL_SHAFT, state, facing);
            kineticRotationTransform(shaft, be, axis, angle, light);
            shaft.renderInto(ms, buffer.getBuffer(RenderType.solid()));
        } else if (isHollowed) {
            var f = true;
            for (var dir : Iterate.directions) {
                if (dir.getAxis() != cog.getRotationAxis(state)) continue;
                var offset = pos.relative(dir);
                while (INewAxisConnection.isValidNewShaft(level.getBlockState(offset))) offset = offset.relative(dir);

                var offPosAlt = pos.relative(dir.getOpposite());
                while (INewAxisConnection.isValidNewShaft(level.getBlockState(offPosAlt))) offPosAlt = offPosAlt.relative(dir.getOpposite());

                if (level.getBlockEntity(offset) instanceof KineticBlockEntity kBE) {
                    var oppDir = dir.getOpposite();
                    var source = kBE.source;
                    if (offPosAlt.equals(source) || kBE.getSpeed() == 0) continue;

                    var kState = kBE.getBlockState();
                    if (!(kState.getBlock() instanceof KineticBlock kBlock) || !kBlock.hasShaftTowards(level, kBE.getBlockPos(), kState, oppDir)) continue;

                    var modifier = RotationPropagatorAccessor.getAxisModifier(kBE, oppDir);
                    if (modifier == 0) break;
                    var additive = 0f;
                    var shaftOffset = true;

                    var render = RenderType.solid();
                    if (0 > modifier && kBE instanceof DirectionalShaftHalvesBlockEntity) {
                        additive += getRotationOffsetForPosition(kBE, pos, axis);
                        shaftOffset = false;
                    }

                    var angle = getAngleForLargeCogShaft(kBE, axis, additive, modifier, shaftOffset);
                    var shaft = CachedBuffers.partialFacingVertical(AllPartialModels.SHAFT, state, facing);
                    kineticRotationTransform(shaft, kBE, axis, angle, light);
                    shaft.renderInto(ms, buffer.getBuffer(render));
                    f = false;
                    break;
                }
            }
            if (f) {
                float angle = getAngleForLargeCogShaft(axis, pos);
                var shaft = CachedBuffers.partialFacingVertical(AllPartialModels.SHAFT, state, facing);
                shaft.light(light);
                shaft.rotateCentered(angle, Direction.get(Direction.AxisDirection.POSITIVE, axis));
                shaft.color(Color.WHITE);
                shaft.renderInto(ms, buffer.getBuffer(RenderType.solid()));
            }
        }
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
    }

    public static float getAngleForLargeCogShaft(Direction.Axis axis, BlockPos pos) {
        return (getShaftAngleOffset(axis, pos) % 360) / 180 * (float) Math.PI;
    }

    public static float getAngleForLargeCogShaft(KineticBlockEntity be, Direction.Axis axis, float additive, float modifier, boolean offset) {
        return ((AnimationTickHolder.getRenderTime(be.getLevel()) * (be.getSpeed() * modifier) * 3f / 10 + (offset ? getShaftAngleOffset(axis, be.getBlockPos()) : 0) + additive) % 360) / 180 * (float) Math.PI;
    }

    public static float getShaftAngleOffset(Direction.Axis axis, BlockPos pos) {
        var offset = 0f;
        var d = (((axis == Direction.Axis.X) ? 0f : pos.getX()) + ((axis == Direction.Axis.Y) ? 0f : pos.getY()) + ((axis == Direction.Axis.Z) ? 0f : pos.getZ())) % 2f;
        if (d == 0f) offset = 22.5f;
        return offset;
    }
}
