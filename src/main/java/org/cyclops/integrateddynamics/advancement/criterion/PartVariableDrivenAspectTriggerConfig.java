package org.cyclops.integrateddynamics.advancement.criterion;

import org.cyclops.cyclopscore.config.extendedconfig.CriterionTriggerConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 *
 */
public class PartVariableDrivenAspectTriggerConfig extends CriterionTriggerConfig<PartVariableDrivenAspectTrigger.Instance> {

    public PartVariableDrivenAspectTriggerConfig() {
        super(
                IntegratedDynamics._instance,
                "part_variable_driven",
                new PartVariableDrivenAspectTrigger()
        );
    }

}
