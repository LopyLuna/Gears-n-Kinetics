package dev.lopyluna.gnkinetics;

import com.google.gson.JsonElement;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.tterrag.registrate.providers.ProviderType;
import dev.lopyluna.gnkinetics.content.datagen.DatagenTags;
import dev.lopyluna.gnkinetics.content.datagen.GearsRecipeProvider;
import dev.lopyluna.gnkinetics.content.datagen.recipes.VanillaRecipeGen;
import dev.lopyluna.gnkinetics.register.GearsSoundEvents;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Map;
import java.util.function.BiConsumer;

import static dev.lopyluna.gnkinetics.Gears.MOD_ID;

public class GearsDatagen {
    public static void gatherDataHighPriority(GatherDataEvent event) {
        if (event.getMods().contains(MOD_ID)) addExtraRegistrateData();
    }

    @SuppressWarnings("all")
    public static void gatherData(GatherDataEvent event) {
        if (!event.getMods().contains(MOD_ID)) return;
        var gen = event.getGenerator();
        var output = gen.getPackOutput();
        var provider = event.getLookupProvider();
        var helper = event.getExistingFileHelper();
        gen.addProvider(event.includeClient(), GearsSoundEvents.provider(gen));

        gen.addProvider(event.includeServer(), new VanillaRecipeGen(output, provider));
        //gen.addProvider(event.includeServer(), new MechanicalCraftingGen(output, provider));
        if (event.includeServer()) GearsRecipeProvider.registerAllProcessing(gen, output, provider);
    }


    private static void addExtraRegistrateData() {
        DatagenTags.addGenerators();
        Gears.REG.addDataGenerator(ProviderType.LANG, provider -> {
            BiConsumer<String, String> langConsumer = provider::add;

            //provideDefaultLang("interface", langConsumer);
            //provideDefaultLang("tooltips", langConsumer);
            GearsSoundEvents.provideLang(langConsumer);
        });
    }

    private static void provideDefaultLang(String fileName, BiConsumer<String, String> consumer) {
        var path = "assets/"+ MOD_ID +"/lang/default/" + fileName + ".json";
        var jsonElement = FilesHelper.loadJsonResource(path);
        if (jsonElement == null) throw new IllegalStateException(String.format("Could not find default lang file: %s", path));
        for (Map.Entry<String, JsonElement> entry : jsonElement.getAsJsonObject().entrySet()) consumer.accept(entry.getKey(), entry.getValue().getAsString());
    }
}
