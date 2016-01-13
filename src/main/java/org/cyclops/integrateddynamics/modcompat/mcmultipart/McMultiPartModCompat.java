package org.cyclops.integrateddynamics.modcompat.mcmultipart;

import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.modcompat.IModCompat;
import org.cyclops.integrateddynamics.Reference;

/**
 * Mod compat for the McMultiPart mod
 * @author rubensworks
 */
public class McMultiPartModCompat implements IModCompat {
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
        if(step == Step.INIT) {
            if(MinecraftHelpers.isClientSide()) {
                McMultiPartHelpers.loadClient();
            }
            McMultiPartHelpers.load();
        }
    }
}
