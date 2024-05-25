package org.cyclops.integrateddynamics.advancement.criterion;

import org.cyclops.cyclopscore.config.extendedconfig.CriterionTriggerConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 *
 */
public class NetworkInitializedTriggerConfig extends CriterionTriggerConfig<NetworkInitializedTrigger.Instance> {

    public NetworkInitializedTriggerConfig() {
        super(
                IntegratedDynamics._instance,
                "network_initialized",
                new NetworkInitializedTrigger()
        );
    }

}
