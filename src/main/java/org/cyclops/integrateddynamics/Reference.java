package org.cyclops.integrateddynamics;

/**
 * Class that can hold basic static things that are better not hard-coded
 * like mod details, texture paths, ID's...
 * @author rubensworks
 */
public final class Reference {

    // Mod info
    public static final String MOD_ID = "integrateddynamics";
    public static final String MOD_NAME = "Integrated Dynamics";
    public static final String MOD_VERSION = "@VERSION@";
    public static final String MOD_BUILD_NUMBER = "@BUILD_NUMBER@";
    public static final String MOD_MC_VERSION = "@MC_VERSION@";
    public static final String GA_TRACKING_ID = "UA-65307010-4";
    public static final String VERSION_URL = "https://raw.githubusercontent.com/CyclopsMC/Versions/master/1.10/IntegratedDynamics.txt";

    // Biome ID's
    public static final int BIOME_MENEGLIN = 193;

    // OREDICT NAMES
    public static final String DICT_WOODLOG = "logWood";
    public static final String DICT_TREELEAVES = "treeLeaves";
    public static final String DICT_SAPLINGTREE = "treeSapling";
    public static final String DICT_WOODPLANK = "plankWood";

    // MOD ID's
    public static final String MOD_FORGE = "Forge";
    public static final String MOD_FORGE_VERSION = "@FORGE_VERSION@";
    public static final String MOD_FORGE_VERSION_MIN = "12.18.2.2116";
    public static final String MOD_CYCLOPSCORE = "cyclopscore";
    public static final String MOD_CYCLOPSCORE_VERSION = "@CYCLOPSCORE_VERSION@";
    public static final String MOD_CYCLOPSCORE_VERSION_MIN = "0.8.0";
    public static final String MOD_COMMONCAPABILITIES = "commoncapabilities";
    public static final String MOD_COMMONCAPABILITIES_VERSION_MIN = "1.2.0";
    public static final String MOD_CHARSETPIPES = "CharsetPipes";
    public static final String MOD_MCMULTIPART = "mcmultipart";
    public static final String MOD_WAILA = "Waila";
    public static final String MOD_THAUMCRAFT = "Thaumcraft";
    public static final String MOD_JEI = "JEI";
    public static final String MOD_RF_API = "CoFHAPI";
    public static final String MOD_TESLA = "tesla";
    public static final String MOD_TCONSTRUCT = "tconstruct";
    public static final String MOD_FORESTRY = "forestry";
    public static final String MOD_IC2 = "IC2";
    public static final String MOD_TOP = "theoneprobe";
    public static final String MOD_REFINEDSTORAGE = "refinedstorage";

    public static final String MOD_DEPENDENCIES =
            "required-after:" + MOD_FORGE       + "@[" + MOD_FORGE_VERSION_MIN       + ",);" +
            "required-after:" + MOD_CYCLOPSCORE + "@[" + MOD_CYCLOPSCORE_VERSION_MIN + ",);" +
            "required-after:" + MOD_COMMONCAPABILITIES + "@[" + MOD_COMMONCAPABILITIES_VERSION_MIN + ",);";

}
