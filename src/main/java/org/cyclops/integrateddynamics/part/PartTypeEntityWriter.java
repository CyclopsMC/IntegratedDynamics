package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Lists;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.part.write.PartStateWriterBase;
import org.cyclops.integrateddynamics.core.part.write.PartTypeWriteBase;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * An entity writer part.
 * @author josephcsible
 */
public class PartTypeEntityWriter extends PartTypeWriteBase<PartTypeEntityWriter, PartStateWriterBase<PartTypeEntityWriter>> {

    public PartTypeEntityWriter(String name) {
        super(name);
        AspectRegistry.getInstance().register(this, Lists.<IAspect>newArrayList(

        ));
    }

    @Override
    public PartStateWriterBase<PartTypeEntityWriter> constructDefaultState() {
        return new PartStateWriterBase<PartTypeEntityWriter>(Aspects.REGISTRY.getAspects(this).size());
    }
    
    @Override
    public int getConsumptionRate(PartStateWriterBase<PartTypeEntityWriter> state) {
        return GeneralConfig.entityWriterBaseConsumption;
    }

}
