package org.cyclops.integrateddynamics.modcompat.mcmultipart;

import org.cyclops.cyclopscore.modcompat.IModCompat;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;

/**
 * Mod compat for the McMultiPart mod
 * @author rubensworks
 */
public class McMultiPartModCompat implements IModCompat {

    public static boolean ENABLED = false;

    @Override
    public String getModID() {
        return Reference.MOD_MCMULTIPART;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getComment() {
        return "Multipart support for cables and parts";
    }

    @Override
    public void onInit(Step step) {
        ENABLED = true;
        IntegratedDynamics._instance.getCapabilityConstructorRegistry()
                .registerTile(TileMultipartTicking.class, new MultipartTileCompat());
    }
}
