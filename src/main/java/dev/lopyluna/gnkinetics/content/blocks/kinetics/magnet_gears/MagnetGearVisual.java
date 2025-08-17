package dev.lopyluna.gnkinetics.content.blocks.kinetics.magnet_gears;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import dev.lopyluna.gnkinetics.register.client.GearsPartialModels;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class MagnetGearVisual extends KineticBlockEntityVisual<SimpleKineticBlockEntity> {
    public static MagnetGearVisual small(VisualizationContext modelManager, SimpleKineticBlockEntity be, float partialTick) {
        return new MagnetGearVisual(modelManager, be, partialTick, Models.partial(GearsPartialModels.SHAFTLESS_MAGNET_GEAR));
    }
    public static MagnetGearVisual large(VisualizationContext modelManager, SimpleKineticBlockEntity be, float partialTick) {
        return new MagnetGearVisual(modelManager, be, partialTick, Models.partial(GearsPartialModels.SHAFTLESS_LARGE_MAGNET_GEAR));
    }

    protected final RotatingInstance rotatingModel;

    public MagnetGearVisual(VisualizationContext modelManager, SimpleKineticBlockEntity be, float partialTick, Model model) {
        super(modelManager, be, partialTick);
        rotatingModel = instancerProvider().instancer(AllInstanceTypes.ROTATING, model).createInstance();
        rotatingModel.setup(be).setPosition(getVisualPosition()).rotateToFace(rotationAxis()).setChanged();
    }

    @Override
    public void update(float pt) {
        rotatingModel.setup(blockEntity).setChanged();
    }

    @Override
    public void updateLight(float partialTick) {
        relight(rotatingModel);
    }

    @Override
    protected void _delete() {
        rotatingModel.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
        consumer.accept(rotatingModel);
    }
}
