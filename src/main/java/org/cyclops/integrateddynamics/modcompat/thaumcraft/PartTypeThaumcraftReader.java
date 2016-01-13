package org.cyclops.integrateddynamics.modcompat.thaumcraft;

import com.google.common.collect.Sets;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.part.read.PartStateReaderBase;
import org.cyclops.integrateddynamics.core.part.read.PartTypeReadBase;
import org.cyclops.integrateddynamics.modcompat.thaumcraft.aspect.read.AspectReadBooleanThaumcraftIsAspectContainer;
import org.cyclops.integrateddynamics.modcompat.thaumcraft.aspect.read.AspectReadListThaumcraftAspectContainer;

/**
 * An thaumcraft reader part.
 * @author rubensworks
 */
public class PartTypeThaumcraftReader extends PartTypeReadBase<PartTypeThaumcraftReader, PartStateReaderBase<PartTypeThaumcraftReader>> {

    public PartTypeThaumcraftReader(String name) {
        super(name);
        AspectRegistry.getInstance().register(this, Sets.<IAspect>newHashSet(
                new AspectReadBooleanThaumcraftIsAspectContainer(),
                new AspectReadListThaumcraftAspectContainer()
        ));
    }

    @Override
    public PartStateReaderBase<PartTypeThaumcraftReader> constructDefaultState() {
        return new PartStateReaderBase<PartTypeThaumcraftReader>();
    }

}
