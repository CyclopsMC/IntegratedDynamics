package org.cyclops.integrateddynamics.advancement.criterion;

import org.cyclops.cyclopscore.config.extendedconfig.CriterionTriggerConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 *
 */
public class PartWriterAspectTriggerConfig extends CriterionTriggerConfig<PartWriterAspectTrigger.Instance> {

    public PartWriterAspectTriggerConfig() {
        super(
                IntegratedDynamics._instance,
                "part_writer_aspect",
                new PartWriterAspectTrigger()
        );
    }

}
