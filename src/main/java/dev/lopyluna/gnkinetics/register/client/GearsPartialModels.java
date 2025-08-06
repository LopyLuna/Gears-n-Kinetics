package dev.lopyluna.gnkinetics.register.client;

import com.simibubi.create.content.kinetics.gantry.GantryShaftBlock;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.lopyluna.gnkinetics.Gears;
import net.createmod.catnip.data.Iterate;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class GearsPartialModels {
    public static final PartialModel
            COG_CRANK_HANDLE = block("cog_crank/handle"),
            CHAINABLE_COGWHEEL = block("chainable_cogwheel/cogwheel"),
            CHAINABLE_COGWHEEL_CHAINS = block("chainable_cogwheel/chains"),

            SHAFTLESS_ANDESITE_COG = block("shaftless_andesite_cogwheel"),
            LARGE_SHAFTLESS_ANDESITE_COG = block("shaftless_large_andesite_cogwheel"),

            SHAFTLESS_COG_STONE = block("shaftless_cogstone"),
            LARGE_SHAFTLESS_COG_STONE = block("shaftless_large_cogstone"),

            SHAFTLESS_INDUSTRIAL_GEAR = block("shaftless_industrial_gear"),
            LARGE_SHAFTLESS_INDUSTRIAL_GEAR = block("shaftless_large_industrial_gear"),

            HOLLOW_COG = block("hollow_cogwheel"),
            HOLLOW_LARGE_COG = block("hollow_large_cogwheel"),

            HOLLOW_BRASS_GEAR = block("hollow_brass_gear"),
            HOLLOW_LARGE_BRASS_GEAR = block("hollow_large_brass_gear"),

            SHAFTLESS_BRASS_GEAR = block("shaftless_brass_gear"),
            SHAFTLESS_LARGE_BRASS_GEAR = block("shaftless_large_brass_gear"),

            TINY_COG = block("tiny_cogwheel"),
            SHAFTLESS_TINY_COG = block("shaftless_tiny_cogwheel"),

            TINY_BRASS_COG = block("tiny_brass_gear"),
            SHAFTLESS_TINY_BRASS_COG = block("shaftless_tiny_brass_gear"),

            RING_GEAR = block("ring_gear/block")
    ;

    public static final Map<WormGearKey, PartialModel> WORM_GEARS = new HashMap<>();

    static {
        for (var flipped : Iterate.trueAndFalse) for (var part : GantryShaftBlock.Part.values()) {
            var key = new WormGearKey(part, flipped);
            WORM_GEARS.put(key, PartialModel.of(key.name()));
        }
    }

    public record WormGearKey(GantryShaftBlock.Part part, boolean flipped) {
        private ResourceLocation name() {
            var partName = part.getSerializedName();
            if (!flipped) return Gears.loc("block/worm_gear/block_" + partName);
            return Gears.loc("block/worm_gear_" + partName  + "_flipped");
        }
    }

    private static PartialModel block(String path) {
        return PartialModel.of(Gears.loc("block/" + path));
    }

    public static void init() {
    }
}
