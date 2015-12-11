package org.cyclops.integrateddynamics.modcompat.charset;

import com.google.common.collect.Sets;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.part.read.PartStateReaderBase;
import org.cyclops.integrateddynamics.core.part.read.PartTypeReadBase;
import org.cyclops.integrateddynamics.modcompat.charset.aspect.read.AspectReadBooleanCharsetPipesPipeHasContents;
import org.cyclops.integrateddynamics.modcompat.charset.aspect.read.AspectReadItemStackCharsetPipesPipeContents;

/**
 * A charset reader part.
 * @author rubensworks
 */
public class PartTypeCharsetReader extends PartTypeReadBase<PartTypeCharsetReader, PartStateReaderBase<PartTypeCharsetReader>> {

    public PartTypeCharsetReader(String name) {
        super(name);
        AspectRegistry.getInstance().register(this, Sets.<IAspect>newHashSet(
                new AspectReadBooleanCharsetPipesPipeHasContents(),
                new AspectReadItemStackCharsetPipesPipeContents()
        ));
    }

    @Override
    public boolean isSolid(PartStateReaderBase<PartTypeCharsetReader> state) {
        return true;
    }

    @Override
    public PartStateReaderBase<PartTypeCharsetReader> constructDefaultState() {
        return new PartStateReaderBase<PartTypeCharsetReader>();
    }

}
