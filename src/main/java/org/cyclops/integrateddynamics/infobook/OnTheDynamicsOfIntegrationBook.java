package org.cyclops.integrateddynamics.infobook;

import org.cyclops.cyclopscore.infobook.InfoBook;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Infobook class for the On the Dynamics of Integration.
 * @author rubensworks
 */
public class OnTheDynamicsOfIntegrationBook extends InfoBook {

    private static OnTheDynamicsOfIntegrationBook _instance = null;

    private OnTheDynamicsOfIntegrationBook() {
        super(IntegratedDynamics._instance, 2);
    }

    public static OnTheDynamicsOfIntegrationBook getInstance() {
        if(_instance == null) {
            _instance = new OnTheDynamicsOfIntegrationBook();
        }
        return _instance;
    }
}
