package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Sets;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.part.read.PartStateReaderBase;
import org.cyclops.integrateddynamics.core.part.read.PartTypeReadBase;

/**
 * A machine reader part.
 * @author rubensworks
 */
public class PartTypeMachineReader extends PartTypeReadBase<PartTypeMachineReader, PartStateReaderBase<PartTypeMachineReader>> {

    public PartTypeMachineReader(String name) {
        super(name);
        AspectRegistry.getInstance().register(this, Sets.<IAspect>newHashSet(

        ));
    }

    @Override
    public PartStateReaderBase<PartTypeMachineReader> constructDefaultState() {
        return new PartStateReaderBase<PartTypeMachineReader>();
    }

}
