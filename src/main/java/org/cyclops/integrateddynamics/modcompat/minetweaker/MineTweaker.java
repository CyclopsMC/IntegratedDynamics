package org.cyclops.integrateddynamics.modcompat.minetweaker;

import minetweaker.MineTweakerAPI;
import org.cyclops.integrateddynamics.modcompat.minetweaker.handlers.DryingBasinHandler;
import org.cyclops.integrateddynamics.modcompat.minetweaker.handlers.SqueezerHandler;

/**
 * @author rubensworks
 */
public class MineTweaker {

    public static void register() {
        MineTweakerAPI.registerClass(DryingBasinHandler.class);
        MineTweakerAPI.registerClass(SqueezerHandler.class);
    }

}
