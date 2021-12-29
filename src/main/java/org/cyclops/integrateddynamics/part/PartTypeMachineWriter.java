package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Lists;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.part.write.PartStateWriterBase;
import org.cyclops.integrateddynamics.core.part.write.PartTypeWriteBase;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * A machine writer part.
 * @author josephcsible
 */
public class PartTypeMachineWriter extends PartTypeWriteBase<PartTypeMachineWriter, PartStateWriterBase<PartTypeMachineWriter>> {

    public PartTypeMachineWriter(String name) {
        super(name);
        AspectRegistry.getInstance().register(this, Lists.<IAspect>newArrayList(

        ));
    }

    @Override
    public PartStateWriterBase<PartTypeMachineWriter> constructDefaultState() {
        return new PartStateWriterBase<PartTypeMachineWriter>(Aspects.REGISTRY.getAspects(this).size());
    }

    @Override
    public int getConsumptionRate(PartStateWriterBase<PartTypeMachineWriter> state) {
        return GeneralConfig.machineWriterBaseConsumption;
    }

}
