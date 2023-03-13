package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Lists;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.part.write.PartStateWriterBase;
import org.cyclops.integrateddynamics.core.part.write.PartTypeWriteBase;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * A redstone writer part.
 * @author rubensworks
 */
public class PartTypeRedstoneWriter extends PartTypeWriteBase<PartTypeRedstoneWriter, PartStateWriterBase<PartTypeRedstoneWriter>> {

    public PartTypeRedstoneWriter(String name) {
        super(name);
        AspectRegistry.getInstance().register(this, Lists.<IAspect>newArrayList(
                Aspects.Write.Redstone.BOOLEAN,
                Aspects.Write.Redstone.INTEGER,
                Aspects.Write.Redstone.BOOLEAN_PULSE,
                Aspects.Write.Redstone.INTEGER_PULSE
        ));
    }

    @Override
    public boolean supportsOffsets() {
        return false;
    }

    @Override
    public PartStateWriterBase<PartTypeRedstoneWriter> constructDefaultState() {
        return new PartStateWriterBase<PartTypeRedstoneWriter>(Aspects.REGISTRY.getAspects(this).size());
    }

    @Override
    public int getConsumptionRate(PartStateWriterBase<PartTypeRedstoneWriter> state) {
        return GeneralConfig.redstoneWriterBaseConsumption;
    }

}
