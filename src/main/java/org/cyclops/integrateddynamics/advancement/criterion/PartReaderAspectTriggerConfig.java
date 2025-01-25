package org.cyclops.integrateddynamics.advancement.criterion;

import org.cyclops.cyclopscore.config.extendedconfig.CriterionTriggerConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 *
 */
public class PartReaderAspectTriggerConfig extends CriterionTriggerConfigCommon<PartReaderAspectTrigger.Instance, IntegratedDynamics> {

    public PartReaderAspectTriggerConfig() {
        super(
                IntegratedDynamics._instance,
                "part_reader_aspect",
                new PartReaderAspectTrigger()
        );
    }

}
