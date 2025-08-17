package dev.lopyluna.gnkinetics.register;

import dev.lopyluna.gnkinetics.Gears;

import static dev.lopyluna.gnkinetics.Gears.REG;

@SuppressWarnings("unused")
public class GearsLangPartial {
    public static void addTranslations() {
        var cc = "chainable_cogwheel";

        REG.addRawLang(desc(cc, "already_connected"), "Cogwheels are already connected");
        REG.addRawLang(desc(cc, "blocks_invalid"), "Cogwheels blocks invalid, Sneak-click to reset");
        REG.addRawLang(desc(cc, "cannot_add_more_connections"), "Cannot add more connections to this Cogwheel");
        REG.addRawLang(desc(cc, "cannot_connect_vertically"), "Chains cannot be tilted in the axis direction");
        REG.addRawLang(desc(cc, "cannot_connect_axis"), "Cannot connect axis cogwheels");
        REG.addRawLang(desc(cc, "not_enough_chains"), "Not holding enough chains");
        REG.addRawLang(desc(cc, "select_second"), "Select a second cogwheel to connect");
        REG.addRawLang(desc(cc, "selection_cleared"), "Selection Cleared");
        REG.addRawLang(desc(cc, "too_close"), "Too close together");
        REG.addRawLang(desc(cc, "too_far"), "Too far apart");
        REG.addRawLang(desc(cc, "too_steep"), "Slope too steep");
        REG.addRawLang(desc(cc, "valid_connection"), "Can Connect ✔");
    }

    public static String desc(String target, String type) {
        return newLang("", "", target, type);
    }

    public static String recipe(String type) {
        return newLang("", "", "recipe", type);
    }
    public static String item(String type) {
        return newLang("item", "", "", type);
    }
    public static String block(String type) {
        return newLang("block", "", "", type);
    }
    public static String biome(String type) {
        return newLang("biome", "", "", type);
    }
    public static String newLang(String prefix, String prefixType, String suffix, String suffixType) {
        var lang = Gears.MOD_ID;
        if (!prefix.isEmpty()) lang = prefix + "." + lang;
        if (!prefixType.isEmpty()) lang = prefixType + "." + lang;
        if (!suffix.isEmpty()) lang = lang + "." + suffix;
        if (!suffixType.isEmpty()) lang = lang + "." + suffixType;
        return lang;
    }
}