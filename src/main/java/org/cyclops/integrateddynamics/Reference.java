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
    public static final String MOD_FINGERPRINT = "@FINGERPRINT@";
    public static final String GA_TRACKING_ID = "UA-65307010-4";
    public static final String VERSION_URL = "https://raw.githubusercontent.com/CyclopsMC/Versions/master/1.12/IntegratedDynamics.txt";
    public static final String BOOK_URL = "https://integrateddynamics.rubensworks.net/book/";

    // Paths
    public static final String TEXTURE_PATH_GUI = "textures/gui/";

    // Biome ID's
    public static final int BIOME_MENEGLIN = 193;

    // OREDICT NAMES
    public static final String DICT_WOODLOG = "logWood";
    public static final String DICT_TREELEAVES = "treeLeaves";
    public static final String DICT_SAPLINGTREE = "treeSapling";
    public static final String DICT_WOODPLANK = "plankWood";
    public static final String DICT_TORCH = "torch";
    public static final String DICT_STAIRWOOD = "stairWood";

    // MOD ID's
    public static final String MOD_FORGE = "forge";
    public static final String MOD_FORGE_VERSION = "@FORGE_VERSION@";
    public static final String MOD_FORGE_VERSION_MIN = "14.23.5.2768";
    public static final String MOD_CYCLOPSCORE = "cyclopscore";
    public static final String MOD_CYCLOPSCORE_VERSION = "@CYCLOPSCORE_VERSION@";
    public static final String MOD_CYCLOPSCORE_VERSION_MIN = "1.6.5";
    public static final String MOD_COMMONCAPABILITIES = "commoncapabilities";
    public static final String MOD_COMMONCAPABILITIES_VERSION_MIN = "2.4.4";
    public static final String MOD_CHARSETPIPES = "charsetpipes";
    public static final String MOD_MCMULTIPART = "mcmultipart";
    public static final String MOD_WAILA = "Waila";
    public static final String MOD_THAUMCRAFT = "Thaumcraft";
    public static final String MOD_JEI = "jei";
    public static final String MOD_TESLA = "tesla";
    public static final String MOD_TCONSTRUCT = "tconstruct";
    public static final String MOD_FORESTRY = "forestry";
    public static final String MOD_IC2 = "ic2";
    public static final String MOD_TOP = "theoneprobe";
    public static final String MOD_REFINEDSTORAGE = "refinedstorage";
    public static final String MOD_IMMERSIVEENGINEERING = "immersiveengineering";
    public static final String MOD_MINETWEAKER = "MineTweaker3";

    public static final String MOD_DEPENDENCIES =
            "required-after:" + MOD_FORGE       + "@[" + MOD_FORGE_VERSION_MIN       + ",);" +
            "required-after:" + MOD_CYCLOPSCORE + "@[" + MOD_CYCLOPSCORE_VERSION_MIN + ",);" +
            "required-after:" + MOD_COMMONCAPABILITIES + "@[" + MOD_COMMONCAPABILITIES_VERSION_MIN + ",);";

}
