package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Sets;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.write.DefaultPartStateWriter;
import org.cyclops.integrateddynamics.core.part.write.PartTypeWriteBase;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * A redstone writer part.
 * @author rubensworks
 */
public class PartTypeRedstoneWriter extends PartTypeWriteBase<PartTypeRedstoneWriter, DefaultPartStateWriter<PartTypeRedstoneWriter>> {

    public PartTypeRedstoneWriter(String name) {
        super(name);
        AspectRegistry.getInstance().register(this, Sets.<IAspect>newHashSet(
                Aspects.WRITE_BOOLEAN_REDSTONE,
                Aspects.WRITE_INTEGER_REDSTONE
        ));
    }

    @Override
    public DefaultPartStateWriter<PartTypeRedstoneWriter> constructDefaultState() {
        return new DefaultPartStateWriter<PartTypeRedstoneWriter>(Aspects.REGISTRY.getAspects(this).size());
    }

}
