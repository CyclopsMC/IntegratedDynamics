package org.cyclops.integrateddynamics.advancement.criterion;

import org.cyclops.cyclopscore.config.extendedconfig.CriterionTriggerConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 *
 */
public class PartReaderAspectTriggerConfig extends CriterionTriggerConfig<PartReaderAspectTrigger.Instance> {

    public PartReaderAspectTriggerConfig() {
        super(
                IntegratedDynamics._instance,
                "part_reader_aspect",
                new PartReaderAspectTrigger()
        );
    }

}
