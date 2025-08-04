package dev.lopyluna.gnkinetics;

import com.mojang.logging.LogUtils;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import dev.lopyluna.gnkinetics.content.utils.GearsRegistry;
import dev.lopyluna.gnkinetics.events.CommonEvents;
import dev.lopyluna.gnkinetics.register.*;
import net.createmod.catnip.lang.FontHelper;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import static dev.lopyluna.gnkinetics.register.GearsCreativeTabs.BASE_TAB;

@Mod(Gears.MOD_ID)
public class Gears {
    public static final String FULL_NAME = "Gears n' Kinetics";
    public static final String NAME = "GnKinetics";
    public static final String MOD_ID = "gnkinetics";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static GearsRegistry REGISTER = new GearsRegistry(MOD_ID);
    public static CreateRegistrate REG = CreateRegistrate.create(MOD_ID);

    static {
        REG.setTooltipModifierFactory(item -> new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE).andThen(TooltipModifier.mapNull(create(item))));
    }

    public Gears(IEventBus modEventBus, ModContainer modContainer) {
        REGISTER.register(modEventBus);
        GearsCreativeTabs.register();
        var context = ModLoadingContext.get();
        REG.registerEventListeners(modEventBus);
        REG.defaultCreativeTab(BASE_TAB, "base_tab");

        GearsSoundEvents.prepare();
        GearsLangPartial.addTranslations();
        GearsTags.init();
        GearsItems.register();
        GearsBlocks.register();
        GearsBETypes.register();
        GearsRotationPropagation.register();
        GearsPackets.register();

        GearsConfigs.register(context, modContainer);

        modEventBus.addListener(CommonEvents::commonSetup);
        modEventBus.addListener(GearsCreativeTabs::addCreative);
        modEventBus.addListener(EventPriority.HIGHEST, GearsDatagen::gatherDataHighPriority);
        modEventBus.addListener(EventPriority.LOWEST, GearsDatagen::gatherData);
        modEventBus.addListener(GearsSoundEvents::register);
    }

    public static LangBuilder lang() {
        return new LangBuilder(MOD_ID);
    }
    public static ResourceLocation loc(String loc) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, loc);
    }
    public static ResourceLocation emptyLoc() {
        return loc("empty");
    }

    @Nullable
    public static KineticStats create(Item item) {
        if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof Block block)
            if (block instanceof IRotate) return new KineticStats(block);
        return null;
    }
}
