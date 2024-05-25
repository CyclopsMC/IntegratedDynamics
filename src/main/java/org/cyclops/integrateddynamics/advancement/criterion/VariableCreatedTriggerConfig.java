package org.cyclops.integrateddynamics.advancement.criterion;

import org.cyclops.cyclopscore.config.extendedconfig.CriterionTriggerConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 *
 */
public class VariableCreatedTriggerConfig extends CriterionTriggerConfig<VariableCreatedTrigger.Instance> {

    public VariableCreatedTriggerConfig() {
        super(
                IntegratedDynamics._instance,
                "variable_created",
                new VariableCreatedTrigger()
        );
    }

}
