package dev.lopyluna.gnkinetics.content.blocks.kinetics.planetary_gear;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.KineticDebugger;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.AbstractInstance;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visual.TickableVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.lopyluna.gnkinetics.mixins.KineticBlockEntityAccessor;
import dev.lopyluna.gnkinetics.mixins.KineticEffectHandlerAccessor;
import dev.lopyluna.gnkinetics.register.client.GearsPartialModels;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.theme.Color;
import net.minecraft.core.Direction;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PlanetaryGearVisual extends SingleAxisRotatingVisual<PlanetaryGearBE> implements SimpleDynamicVisual {
    protected final TransformedInstance rotatingPlanetModelA;
    protected final TransformedInstance rotatingPlanetModelB;
    protected final TransformedInstance rotatingPlanetModelC;
    protected final TransformedInstance rotatingPlanetModelD;
    protected final RotatingInstance rotatingSunModel;

    protected final RotatingInstance rotatingModes;
    protected int mode;
    protected final RotatingInstance shaftA;
    protected final RotatingInstance shaftB;

    protected static final float PLANET_ORBIT_RADIUS = 12f / 16f;

    protected final List<RotatingInstance> rotatingModels = new ArrayList<>();
    protected final List<TransformedInstance> transformedModels = new ArrayList<>();

    public PlanetaryGearVisual(VisualizationContext context, PlanetaryGearBE blockEntity, float partialTick) {
        super(context, blockEntity, partialTick, Models.partial(GearsPartialModels.PLANETARY_RING_GEAR));
        var state = blockEntity.getBlockState();
        var positiveDir = state.getValue(PlanetaryGearBlock.POSITIVE_DIR);
        int mode = state.getValue(PlanetaryGearBlock.MODE);
        var rotAxis = rotationAxis();
        var visualPos = getVisualPosition();

        // Calculate initial speeds based on mode
        float[] speeds = calculateSpeeds(blockEntity.getSpeed(), mode);
        float sunSpeed = speeds[0];
        float shaftASpeed = speeds[2];
        float shaftBSpeed = speeds[3];

        rotatingSunModel = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(GearsPartialModels.PLANETARY_SUN_GEAR))
                .createInstance()
                .rotateToFace(Direction.UP, rotAxis);
                 setup(rotatingSunModel, blockEntity, 0, sunSpeed)
                .setPosition(visualPos);

        rotatingPlanetModelA = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(GearsPartialModels.PLANETARY_PLANET_GEAR))
                .createInstance();
        rotatingPlanetModelB = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(GearsPartialModels.PLANETARY_PLANET_GEAR))
                .createInstance();
        rotatingPlanetModelC = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(GearsPartialModels.PLANETARY_PLANET_GEAR))
                .createInstance();
        rotatingPlanetModelD = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(GearsPartialModels.PLANETARY_PLANET_GEAR))
                .createInstance();

        updatePlanets(partialTick);

        this.mode = mode;
        rotatingModes = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(GearsPartialModels.getPlanetaryMode(mode)))
                .createInstance()
                .rotateToFace(positiveDir ? Direction.UP : Direction.DOWN, rotAxis);
                 setup(rotatingModes, blockEntity, 5, 0f)
                .setPosition(visualPos);

        shaftA = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT_HALF))
                .createInstance()
                .rotateToFace(positiveDir ? Direction.SOUTH : Direction.NORTH, rotAxis);
                 setup(shaftA, blockEntity, 5, shaftASpeed)
                .setPosition(visualPos);

        shaftB = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT_HALF))
                .createInstance()
                .rotateToFace(positiveDir ? Direction.NORTH : Direction.SOUTH, rotAxis);
                 setup(shaftB, blockEntity, 5, shaftBSpeed)
                .setPosition(visualPos);

        rotatingSunModel.setChanged();
        rotatingModes.setChanged();
        shaftA.setChanged();
        shaftB.setChanged();

        rotatingModels.add(rotatingModel);
        rotatingModels.add(rotatingSunModel);
        rotatingModels.add(rotatingModes);
        rotatingModels.add(shaftA);
        rotatingModels.add(shaftB);

        transformedModels.add(rotatingPlanetModelA);
        transformedModels.add(rotatingPlanetModelB);
        transformedModels.add(rotatingPlanetModelC);
        transformedModels.add(rotatingPlanetModelD);
    }

    public RotatingInstance setup(RotatingInstance inst, KineticBlockEntity blockEntity, float offset, float speed) {
        var blockState = blockEntity.getBlockState();
        var axis = KineticBlockEntityVisual.rotationAxis(blockState);
        var pos = blockEntity.getBlockPos();

        var rotOff = KineticBlockEntityVisual.rotationOffset(blockState, axis, pos);
        var off = getOff((int) offset, rotationAxis(), rotOff == 22.5f);

        var instance = inst.setRotationAxis(axis)
                .setRotationalSpeed(speed * RotatingInstance.SPEED_MULTIPLIER)
                .setRotationOffset(rotOff + off + blockEntity.getRotationAngleOffset(axis));

        if (KineticDebugger.isActive()) instance.setColor(blockEntity);
        return instance;
    }

    private static float getOff(int offset, Direction.Axis rotAxis, boolean should) {
        var x = rotAxis == Direction.Axis.X;
        var y = rotAxis == Direction.Axis.Y;

        return switch (offset) {
            case 0 -> should ? 22.5f : 11.25f; // Sun Gear
            case 1 -> should ? y ? 67.5f : x ? -22.5f : 157.5f : y ? -101.25f : x ? 168.75f : -11.25f; // Planet Gear A
            case 2 -> should ? y ? 157.5f : x ? 67.5f : -112.5f : y ? -11.25f : x ? -101.25f : 78.75f; // Planet Gear B
            case 3 -> should ? y ? -112.5f : x ? 157.5f : -22.5f : y ? 78.75f : x ? -11.25f : 168.75f; // Planet Gear C
            case 4 -> should ? y ? -22.5f : x ? -112.5f : 67.5f : y ? 168.75f : x ? 78.75f : -101.25f; // Planet Gear D
            default -> 0f;
        };
    }

    private static float[] calculateSpeeds(float speed, int mode) {
        float ringSpeed, sunSpeed, carrierSpeed, planetSpeed;

        switch (mode) {
            case 1 -> {
                ringSpeed = speed;
                sunSpeed = 0f;
                carrierSpeed = speed * (2f / 3f);
                planetSpeed = (ringSpeed - carrierSpeed) * (16f / 3f);
            }
            case 2 -> {
                ringSpeed = 0f;
                sunSpeed = speed;
                carrierSpeed = speed * (1f / 3f);
                planetSpeed = (sunSpeed - carrierSpeed) * (8f / 3f);
            }
            default -> {
                ringSpeed = speed;
                sunSpeed = speed * -2f;
                carrierSpeed = 0f;
                planetSpeed = speed * (16f / 3f);
            }
        }

        float shaftASpeed, shaftBSpeed;
        switch (mode) {
            case 1 -> {
                shaftASpeed = ringSpeed;
                shaftBSpeed = carrierSpeed;
            }
            case 2 -> {
                shaftASpeed = sunSpeed;
                shaftBSpeed = carrierSpeed;
            }
            default -> {
                shaftASpeed = ringSpeed;
                shaftBSpeed = sunSpeed;
            }
        }

        return new float[] { sunSpeed, planetSpeed, shaftASpeed, shaftBSpeed, carrierSpeed };
    }

    private void updatePlanets(float partialTick) {
        var state = blockEntity.getBlockState();
        int mode = state.getValue(PlanetaryGearBlock.MODE);
        var speed = blockEntity.getSpeed();
        var rotAxis = rotationAxis();
        var visualPos = getVisualPosition();

        float time = AnimationTickHolder.getRenderTime(blockEntity.getLevel());

        float baseStep = (float) (speed * Math.PI / 600.0);
        float k = (float) 16 / 8;

        float carrierAngleStep, planetSpinStep;

        switch (mode) {
            case 1 -> {
                carrierAngleStep = (k / (1.0f + k)) * baseStep;
                planetSpinStep = (16f / 3f) * (baseStep - carrierAngleStep);
            }
            case 2 -> {
                carrierAngleStep = baseStep / (1.0f + k);
                planetSpinStep = (16f / 3f) * (0f - carrierAngleStep);
            }
            default -> {
                carrierAngleStep = 0f;
                planetSpinStep = (16f / 3f) * baseStep;
            }
        }

        float carrierAngle = time * carrierAngleStep;
        float planetSpin = time * planetSpinStep;

        Direction facing = Direction.get(Direction.AxisDirection.POSITIVE, rotAxis);

        TransformedInstance[] planets = { rotatingPlanetModelA, rotatingPlanetModelB, rotatingPlanetModelC, rotatingPlanetModelD };
        for (int i = 0; i < 4; i++) {
            float angleAround = carrierAngle + i * ((float) Math.PI / 2f);

            float offsetX = 0, offsetY = 0, offsetZ = 0;
            switch (rotAxis) {
                case X -> {
                    offsetY = (float) Math.cos(angleAround) * PLANET_ORBIT_RADIUS;
                    offsetZ = (float) Math.sin(angleAround) * PLANET_ORBIT_RADIUS;
                }
                case Y -> {
                    offsetX = (float) Math.cos(angleAround) * PLANET_ORBIT_RADIUS;
                    offsetZ = (float) Math.sin(angleAround) * PLANET_ORBIT_RADIUS;
                }
                case Z -> {
                    offsetX = (float) Math.cos(angleAround) * PLANET_ORBIT_RADIUS;
                    offsetY = (float) Math.sin(angleAround) * PLANET_ORBIT_RADIUS;
                }
            }

            float totalPlanetRotation = planetSpin + angleAround + (float) Math.PI;
            planets[i].setIdentityTransform()
                    .translate(visualPos.getX() + offsetX, visualPos.getY() + offsetY, visualPos.getZ() + offsetZ)
                    .center()
                    .rotate(totalPlanetRotation, facing)
                    .rotate(new Quaternionf().rotateTo(0, 1, 0, facing.getStepX(), facing.getStepY(), facing.getStepZ()))
                    .uncenter()
                    .setChanged();
        }
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        updatePlanets(ctx.partialTick());
    }

    @Override
    public void update(float pt) {
        var state = blockEntity.getBlockState();
        int mode = state.getValue(PlanetaryGearBlock.MODE);
        var speed = blockEntity.getSpeed();

        float[] speeds = calculateSpeeds(speed, mode);
        float sunSpeed = speeds[0];
        float shaftASpeed = speeds[2];
        float shaftBSpeed = speeds[3];

        float ringSpeed = (mode == 2) ? 0f : speed;

        setup(rotatingModel, blockEntity, 5, ringSpeed).setChanged();
        setup(rotatingSunModel, blockEntity, 0, sunSpeed).setChanged();

        if (this.mode != mode) {
            instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(GearsPartialModels.getPlanetaryMode(mode))).stealInstance(rotatingModes);
            this.mode = mode;
        }


        setup(rotatingModes, blockEntity, 5, shaftASpeed).setChanged();
        setup(shaftA, blockEntity, 5, shaftASpeed).setChanged();
        setup(shaftB, blockEntity, 5, shaftBSpeed).setChanged();
    }

    @Override
    public void tick(TickableVisual.Context context) {
        float effect = ((KineticEffectHandlerAccessor) ((KineticBlockEntityAccessor) blockEntity).effects()).overStressedEffect();
        if (effect != 0) {
            boolean os = effect > 0;
            Color color = os ? Color.RED : Color.SPRING_GREEN;
            float weight = os ? effect : -effect;

            var colorized = Color.WHITE.mixWith(color, weight);
            rotatingModels.forEach(model -> model.setColor(colorized));
            transformedModels.forEach(model -> model.colorRgb(colorized.getRGB()));
        } else {
            rotatingModels.forEach(model -> model.setColor(Color.WHITE));
            transformedModels.forEach(model -> model.colorRgb(Color.WHITE.getRGB()));
        }

        rotatingModels.forEach(AbstractInstance::setChanged);
        transformedModels.forEach(AbstractInstance::setChanged);
    }

    @Override
    public void updateLight(float partialTick) {
        rotatingModels.forEach(this::relight);
        transformedModels.forEach(this::relight);
    }

    @Override
    protected void _delete() {
        rotatingModels.forEach(Instance::delete);
        transformedModels.forEach(Instance::delete);
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        rotatingModels.forEach(consumer);
        transformedModels.forEach(consumer);
    }
}
