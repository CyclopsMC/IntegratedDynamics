package org.cyclops.integrateddynamics.advancement.criterion;

import org.cyclops.cyclopscore.config.extendedconfig.CriterionTriggerConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 *
 */
public class PartVariableDrivenAspectTriggerConfig extends CriterionTriggerConfigCommon<PartVariableDrivenAspectTrigger.Instance, IntegratedDynamics> {

    public PartVariableDrivenAspectTriggerConfig() {
        super(
                IntegratedDynamics._instance,
                "part_variable_driven",
                new PartVariableDrivenAspectTrigger()
        );
    }

}
