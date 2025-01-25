package org.cyclops.integrateddynamics.advancement.criterion;

import org.cyclops.cyclopscore.config.extendedconfig.CriterionTriggerConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 *
 */
public class NetworkInitializedTriggerConfig extends CriterionTriggerConfigCommon<NetworkInitializedTrigger.Instance, IntegratedDynamics> {

    public NetworkInitializedTriggerConfig() {
        super(
                IntegratedDynamics._instance,
                "network_initialized",
                new NetworkInitializedTrigger()
        );
    }

}
