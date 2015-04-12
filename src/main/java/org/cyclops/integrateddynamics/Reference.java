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

    // MOD ID's
    public static final String MOD_CYCLOPSCORE = "cyclopscore";
    public static final String MOD_CYCLOPSCORE_MINVERSION = "@CYCLOPSCORE_VERSION@";

    public static final String MOD_DEPENDENCIES =
              "required-after:Forge@[11.14.1.1329,);"
            + "required-after:" + Reference.MOD_CYCLOPSCORE + ""; // TODO: add min version requirements

}
