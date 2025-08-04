package dev.lopyluna.gnkinetics.content.blocks.kinetics.custom_cogs;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.*;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.INewAxisConnection;
import net.createmod.catnip.data.Iterate;

import java.util.function.Consumer;

public class CustomCogWheelVisual extends SingleAxisRotatingVisual<BracketedKineticBlockEntity> {
    protected boolean hasShaft;
    protected boolean isHollowed;
    protected RotatingInstance shaft;

    public CustomCogWheelVisual(VisualizationContext context, BracketedKineticBlockEntity be, float partialTick) {
        super(context, be, partialTick, getModel(be));
        if (!(blockEntity.getBlockState().getBlock() instanceof CustomCogWheelBlock cog)) return;
        var axis = KineticBlockEntityRenderer.getRotationAxisOf(blockEntity);
        var state = blockEntity.getBlockState();
        hasShaft = cog.cogType.hasShaft;
        isHollowed = cog.hasShaftHollowed(state);

        if (hasShaft) {
            shaft = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.COGWHEEL_SHAFT)).createInstance();
            shaft.rotateToFace(axis).setup(blockEntity)
                    .setRotationOffset(BracketedKineticBlockEntityRenderer.getShaftAngleOffset(axis, pos))
                    .setPosition(getVisualPosition())
                    .setChanged();

        } else if (cog.isHollowed()) {
            shaft = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT)).createInstance();
            if (isHollowed) {
                var f = true;
                for (var dir : Iterate.directions) {
                    if (dir.getAxis() != cog.getRotationAxis(state)) continue;
                    var offset = pos.relative(dir);
                    while (INewAxisConnection.isValidNewShaft(level.getBlockState(offset)))
                        offset = offset.relative(dir);

                    var offPosAlt = pos.relative(dir.getOpposite());
                    while (INewAxisConnection.isValidNewShaft(level.getBlockState(offPosAlt)))
                        offPosAlt = offPosAlt.relative(dir.getOpposite());

                    if (level.getBlockEntity(offset) instanceof KineticBlockEntity kBE) {
                        var source = kBE.source;
                        if (offPosAlt.equals(source) || kBE.getSpeed() == 0) continue;

                        var kState = kBE.getBlockState();
                        if (!(kState.getBlock() instanceof KineticBlock kBlock) || dir.getAxis() != kBlock.getRotationAxis(kState) || !kBlock.hasShaftTowards(level, kBE.getBlockPos(), kState, dir))
                            continue;

                        shaft.rotateToFace(axis).setup(kBE)
                                .setRotationOffset(BracketedKineticBlockEntityRenderer.getShaftAngleOffset(axis, pos))
                                .setPosition(getVisualPosition());
                        f = false;
                        break;
                    }
                }
                if (f) {
                    shaft.rotateToFace(axis)
                            .setRotationOffset(BracketedKineticBlockEntityRenderer.getShaftAngleOffset(axis, pos))
                            .setPosition(getVisualPosition());
                }
            }
            shaft.setChanged();
        }
    }

    @Override
    public void update(float pt) {
        super.update(pt);
        if (shaft != null) {
            if (!(blockEntity.getBlockState().getBlock() instanceof CustomCogWheelBlock cog)) return;
            var state = blockEntity.getBlockState();
            hasShaft = cog.cogType.hasShaft;
            isHollowed = cog.hasShaftHollowed(state);

            if (hasShaft) shaft.setup(blockEntity);
            else if (isHollowed) {
                for (var dir : Iterate.directions) {
                    if (dir.getAxis() != cog.getRotationAxis(state)) continue;
                    var offset = pos.relative(dir);
                    while (INewAxisConnection.isValidNewShaft(level.getBlockState(offset))) offset = offset.relative(dir);

                    var offPosAlt = pos.relative(dir.getOpposite());
                    while (INewAxisConnection.isValidNewShaft(level.getBlockState(offPosAlt))) offPosAlt = offPosAlt.relative(dir.getOpposite());

                    if (level.getBlockEntity(offset) instanceof KineticBlockEntity kBE) {
                        var source = kBE.source;
                        if (offPosAlt.equals(source) || kBE.getSpeed() == 0) continue;

                        var kState = kBE.getBlockState();
                        if (!(kState.getBlock() instanceof KineticBlock kBlock) || dir.getAxis() != kBlock.getRotationAxis(kState) || !kBlock.hasShaftTowards(level, kBE.getBlockPos(), kState, dir)) continue;

                        shaft.setup(kBE);
                        break;
                    }
                }
            }
            shaft.setRotationOffset(BracketedKineticBlockEntityRenderer.getShaftAngleOffset(rotationAxis(), pos)).setChanged();
        }
    }

    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);
        if (shaft != null) relight(shaft);
    }

    @Override
    protected void _delete() {
        super._delete();
        if (shaft != null) shaft.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        if (shaft != null) consumer.accept(shaft);
    }

    public static Model getModel(BracketedKineticBlockEntity be) {
        var state = be.getBlockState();
        PartialModel model;
        if (state.getBlock() instanceof CustomCogWheelBlock cog) model = cog.model;
        else model = ICogWheel.isLargeCog(state) ? AllPartialModels.SHAFTLESS_LARGE_COGWHEEL : AllPartialModels.SHAFTLESS_COGWHEEL;
        return Models.partial(model);
    }
}
