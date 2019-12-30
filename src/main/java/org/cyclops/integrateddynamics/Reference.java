package org.cyclops.integrateddynamics;

import org.cyclops.cyclopscore.helper.MinecraftHelpers;

/**
 * Class that can hold basic static things that are better not hard-coded
 * like mod details, texture paths, ID's...
 * @author rubensworks
 */
public final class Reference {

    // Mod info
    public static final String MOD_ID = "integrateddynamics";
    public static final String GA_TRACKING_ID = "UA-65307010-4";
    public static final String VERSION_URL = "https://raw.githubusercontent.com/CyclopsMC/Versions/master/" + MinecraftHelpers.getMinecraftVersionMajorMinor() + "/IntegratedDynamics.txt";
    public static final String BOOK_URL = "https://integrateddynamics.rubensworks.net/book/";

    // Paths
    public static final String TEXTURE_PATH_GUI = "textures/gui/";

    // MOD ID's
    public static final String MOD_FORGE = "forge";
    public static final String MOD_CYCLOPSCORE = "cyclopscore";
    public static final String MOD_COMMONCAPABILITIES = "commoncapabilities";

}
