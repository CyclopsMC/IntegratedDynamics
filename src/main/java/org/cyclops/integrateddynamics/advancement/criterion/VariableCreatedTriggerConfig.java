package org.cyclops.integrateddynamics.advancement.criterion;

import org.cyclops.cyclopscore.config.extendedconfig.CriterionTriggerConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 *
 */
public class VariableCreatedTriggerConfig extends CriterionTriggerConfigCommon<VariableCreatedTrigger.Instance, IntegratedDynamics> {

    public VariableCreatedTriggerConfig() {
        super(
                IntegratedDynamics._instance,
                "variable_created",
                new VariableCreatedTrigger()
        );
    }

}
