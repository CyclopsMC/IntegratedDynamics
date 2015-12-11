package org.cyclops.integrateddynamics.modcompat.charset;

import com.google.common.collect.Sets;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.part.write.PartStateWriterBase;
import org.cyclops.integrateddynamics.core.part.write.PartTypeWriteBase;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * A charset writer part.
 * @author rubensworks
 */
public class PartTypeCharsetWriter extends PartTypeWriteBase<PartTypeCharsetWriter, PartStateWriterBase<PartTypeCharsetWriter>> {

    public PartTypeCharsetWriter(String name) {
        super(name);
        AspectRegistry.getInstance().register(this, Sets.<IAspect>newHashSet(

        ));
    }

    @Override
    public boolean isSolid(PartStateWriterBase<PartTypeCharsetWriter> state) {
        return true;
    }

    @Override
    public PartStateWriterBase<PartTypeCharsetWriter> constructDefaultState() {
        return new PartStateWriterBase<PartTypeCharsetWriter>(Aspects.REGISTRY.getAspects(this).size());
    }

}
