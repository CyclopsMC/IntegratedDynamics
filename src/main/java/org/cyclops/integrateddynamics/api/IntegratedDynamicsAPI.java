package org.cyclops.integrateddynamics.api;

import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.init.RegistryManager;

/**
 * Main API class for Integrated Dynamics.
 * @author rubensworks
 */
public class IntegratedDynamicsAPI {

    private static ModBase mod;

    static {
        try {
            Class<?> mainClass = Class.forName("org.cyclops.integrateddynamics.IntegratedDynamics");
            mod = (ModBase) mainClass.getField("_instance").get(null);
        } catch (ClassNotFoundException | NoSuchFieldException | ClassCastException | IllegalAccessException e) {
            //e.printStackTrace();
        }
    }

    /**
     * @return If the Integrated Dynamics mod is loaded.
     */
    public static boolean isPresent() {
        return mod != null;
    }

    /**
     * @return The registry manager.
     */
    public static RegistryManager getRegistryManager() {
        return mod.getRegistryManager();
    }

}
