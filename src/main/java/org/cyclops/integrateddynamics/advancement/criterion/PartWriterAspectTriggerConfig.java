package org.cyclops.integrateddynamics.advancement.criterion;

import org.cyclops.cyclopscore.config.extendedconfig.CriterionTriggerConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 *
 */
public class PartWriterAspectTriggerConfig extends CriterionTriggerConfigCommon<PartWriterAspectTrigger.Instance, IntegratedDynamics> {

    public PartWriterAspectTriggerConfig() {
        super(
                IntegratedDynamics._instance,
                "part_writer_aspect",
                new PartWriterAspectTrigger()
        );
    }

}
