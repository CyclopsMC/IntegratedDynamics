package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Lists;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.part.read.PartStateReaderBase;
import org.cyclops.integrateddynamics.core.part.read.PartTypeReadBase;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * An entity reader part.
 * @author rubensworks
 */
public class PartTypeEntityReader extends PartTypeReadBase<PartTypeEntityReader, PartStateReaderBase<PartTypeEntityReader>> {

    public PartTypeEntityReader(String name) {
        super(name);
        AspectRegistry.getInstance().register(this, Lists.<IAspect>newArrayList(
                Aspects.Read.Entity.INTEGER_ITEMFRAMEROTATION,
                Aspects.Read.Entity.LIST_ENTITIES,
                Aspects.Read.Entity.LIST_PLAYERS,
                Aspects.Read.Entity.ENTITY,
                Aspects.Read.Entity.ITEMSTACK_ITEMFRAMECONTENTS
        ));
    }

    @Override
    public PartStateReaderBase<PartTypeEntityReader> constructDefaultState() {
        return new PartStateReaderBase<PartTypeEntityReader>();
    }

}
