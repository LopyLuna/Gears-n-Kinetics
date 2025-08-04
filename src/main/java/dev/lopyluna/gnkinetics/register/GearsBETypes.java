package dev.lopyluna.gnkinetics.register;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.OrientedRotatingVisual;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import dev.engine_room.flywheel.api.visual.BlockEntityVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.cog_crank.CogCrankBE;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.cog_crank.CogCrankRenderer;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.cog_crank.CogCrankVisual;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.creative_gear_motor.CreativeGearMotorBE;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.creative_gear_motor.GearMotorRenderer;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.custom_cogs.CustomCogWheelRenderer;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.ring_gear.RingGearBE;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.tiny_cog.TinyCogBE;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.tiny_cog.TinyCogBlock;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.worm_gear.WormGearBE;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.worm_gear.WormGearBlock;
import dev.lopyluna.gnkinetics.register.client.GearsPartialModels;
import net.minecraft.core.Direction;

import static dev.lopyluna.gnkinetics.Gears.REG;

public class GearsBETypes {

    public static final BlockEntityEntry<WormGearBE> WORM_GEAR = REG
            .blockEntity("worm_gear", WormGearBE::new)
            .visual(() -> GearsBETypes::wormGear, false)
            .validBlocks(GearsBlocks.WORM_GEAR)
            .renderer(() -> KineticBlockEntityRenderer::new)
            .register();

    public static final BlockEntityEntry<CogCrankBE> COG_CRANK = REG
            .blockEntity("cog_crank", CogCrankBE::new)
            .visual(() -> CogCrankVisual::new)
            .validBlocks(GearsBlocks.COG_CRANK, GearsBlocks.LARGE_COG_CRANK)
            .renderer(() -> CogCrankRenderer::new)
            .register();

    public static final BlockEntityEntry<CreativeGearMotorBE> MOTOR = REG
            .blockEntity("motor", CreativeGearMotorBE::new)
            .visual(() -> SingleAxisRotatingVisual.ofZ(AllPartialModels.MECHANICAL_PUMP_COG))
            .validBlocks(GearsBlocks.CREATIVE_GEAR_MOTOR)
            .renderer(() -> GearMotorRenderer::new)
            .register();

    public static final BlockEntityEntry<TinyCogBE> TINY_COG = REG
            .blockEntity("ting_cogwheel", TinyCogBE::new)
            .visual(() -> ofTinyCog(GearsPartialModels.TINY_COG, GearsPartialModels.SHAFTLESS_TINY_COG, GearsPartialModels.TINY_BRASS_COG, GearsPartialModels.SHAFTLESS_TINY_BRASS_COG), false)
            .validBlocks(GearsBlocks.TINY_COG, GearsBlocks.SHAFTLESS_TINY_COG, GearsBlocks.TINY_BRASS_COG, GearsBlocks.SHAFTLESS_TINY_BRASS_COG)
            .renderer(() -> KineticBlockEntityRenderer::new)
            .register();

    public static final BlockEntityEntry<RingGearBE> RING_GEAR = REG
            .blockEntity("ring_gear", RingGearBE::new)
            .visual(() -> SingleAxisRotatingVisual.of(GearsPartialModels.RING_GEAR), false)
            .validBlocks(GearsBlocks.RING_GEAR)
            .renderer(() -> KineticBlockEntityRenderer::new)
            .register();

    public static final BlockEntityEntry<BracketedKineticBlockEntity> CUSTOM_COGWHEELS = REG
            .blockEntity("cogwheel", BracketedKineticBlockEntity::new)
            //.visual(() -> CustomCogWheelVisual::new, true)
            .validBlocks(
                    GearsBlocks.COG_STONE,
                    GearsBlocks.SHAFTLESS_COG_STONE,
                    GearsBlocks.ANDESITE_COG,
                    GearsBlocks.SHAFTLESS_ANDESITE_COG,
                    GearsBlocks.INDUSTRIAL_GEAR,
                    GearsBlocks.LARGE_INDUSTRIAL_GEAR,
                    GearsBlocks.SHAFTLESS_INDUSTRIAL_GEAR,
                    GearsBlocks.LARGE_SHAFTLESS_INDUSTRIAL_GEAR,
                    GearsBlocks.BRASS_GEAR,
                    GearsBlocks.LARGE_BRASS_GEAR,
                    GearsBlocks.HOLLOW_BRASS_GEAR,
                    GearsBlocks.LARGE_HOLLOW_BRASS_GEAR,
                    GearsBlocks.SHAFTLESS_BRASS_GEAR,
                    GearsBlocks.LARGE_SHAFTLESS_BRASS_GEAR,
                    GearsBlocks.HOLLOW_COGWHEEL,
                    GearsBlocks.LARGE_HOLLOW_COGWHEEL,
                    GearsBlocks.SHAFTLESS_COGWHEEL,
                    GearsBlocks.LARGE_SHAFTLESS_COGWHEEL
            )
            .renderer(() -> CustomCogWheelRenderer::new)
            .register();


    public static BlockEntityVisual<? super WormGearBE> wormGear(VisualizationContext visualizationContext, WormGearBE wormGearBE, float partialTick) {
        var blockState = wormGearBE.getBlockState();
        var facing = blockState.getValue(WormGearBlock.FACING);
        var isFlipped = facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE;
        var model = Models.partial(GearsPartialModels.WORM_GEARS.get(new GearsPartialModels.WormGearKey(blockState.getValue(WormGearBlock.PART), isFlipped)));
        return new OrientedRotatingVisual<>(visualizationContext, wormGearBE, partialTick, Direction.UP, facing, model);
    }

    public static <T extends TinyCogBE> SimpleBlockEntityVisualizer.Factory<T> ofTinyCog(PartialModel shaftPartial, PartialModel partial, PartialModel brassShaftPartial, PartialModel brassPartial) {
        return (context, blockEntity, partialTick) -> {
            var hasShaft = blockEntity.getBlockState().getBlock() instanceof TinyCogBlock cog && cog.hasShaft;
            var brass = blockEntity.isBrass();
            var model = hasShaft ? brass ? brassShaftPartial : shaftPartial : brass ? brassPartial : partial;
            return new SingleAxisRotatingVisual<>(context, blockEntity, partialTick,
                    Models.partial(model));
        };
    }

    public static void register() {}
}
