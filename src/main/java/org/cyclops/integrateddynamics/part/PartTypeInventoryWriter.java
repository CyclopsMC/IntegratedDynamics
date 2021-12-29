package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Lists;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.part.write.PartStateWriterBase;
import org.cyclops.integrateddynamics.core.part.write.PartTypeWriteBase;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * An inventory writer part.
 * @author rubensworks
 */
public class PartTypeInventoryWriter extends PartTypeWriteBase<PartTypeInventoryWriter, PartStateWriterBase<PartTypeInventoryWriter>> {

    public PartTypeInventoryWriter(String name) {
        super(name);
        AspectRegistry.getInstance().register(this, Lists.<IAspect>newArrayList(

        ));
    }

    @Override
    public PartStateWriterBase<PartTypeInventoryWriter> constructDefaultState() {
        return new PartStateWriterBase<PartTypeInventoryWriter>(Aspects.REGISTRY.getAspects(this).size());
    }

    @Override
    public int getConsumptionRate(PartStateWriterBase<PartTypeInventoryWriter> state) {
        return GeneralConfig.inventoryWriterBaseConsumption;
    }

}
