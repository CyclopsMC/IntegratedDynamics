package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Sets;
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
        AspectRegistry.getInstance().register(this, Sets.<IAspect>newHashSet(
                Aspects.WRITE_BOOLEAN_REDSTONE,
                Aspects.WRITE_INTEGER_REDSTONE
        ));
    }

    @Override
    public PartStateWriterBase<PartTypeRedstoneWriter> constructDefaultState() {
        return new PartStateWriterBase<PartTypeRedstoneWriter>(Aspects.REGISTRY.getAspects(this).size());
    }

}
