package org.cyclops.integrateddynamics.modcompat.waila;

import mcmultipart.multipart.IMultipartContainer;
import mcp.mobius.waila.api.IWailaRegistrar;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.part.IPartContainerFacade;

/**
 * Waila support class.
 * @author rubensworks
 *
 */
public class Waila {
    
    /**
     * Waila callback.
     * @param registrar The Waila registrar.
     */
    public static void callbackRegister(IWailaRegistrar registrar){
        registrar.addConfig(Reference.MOD_NAME, getPartConfigId(), L10NHelpers.localize("gui." + Reference.MOD_ID + ".waila.partConfig"));
        registrar.registerBodyProvider(new PartDataProvider(), IPartContainerFacade.class);
        registrar.registerBodyProvider(new PartDataProvider(), IMultipartContainer.class);
    }
    
    /**
     * Config ID.
     * @return The config ID.
     */
    public static String getPartConfigId() {
        return Reference.MOD_ID + ".tank";
    }
    
}
