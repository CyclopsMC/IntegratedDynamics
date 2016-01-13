package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Sets;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.part.read.PartStateReaderBase;
import org.cyclops.integrateddynamics.core.part.read.PartTypeReadBase;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * An inventory reader part.
 * @author rubensworks
 */
public class PartTypeInventoryReader extends PartTypeReadBase<PartTypeInventoryReader, PartStateReaderBase<PartTypeInventoryReader>> {

    public PartTypeInventoryReader(String name) {
        super(name);
        AspectRegistry.getInstance().register(this, Sets.<IAspect>newHashSet(
                Aspects.READ_BOOLEAN_INVENTORY_FULL,
                Aspects.READ_BOOLEAN_INVENTORY_EMPTY,
                Aspects.READ_BOOLEAN_INVENTORY_NONEMPTY,
                Aspects.READ_BOOLEAN_INVENTORY_APPLICABLE,
                Aspects.READ_INTEGER_INVENTORY_COUNT,
                Aspects.READ_LIST_INVENTORY_ITEMSTACKS,
                Aspects.READ_OBJECT_ITEM_STACK_INVENTORY_SLOT
        ));
    }

    @Override
    public PartStateReaderBase<PartTypeInventoryReader> constructDefaultState() {
        return new PartStateReaderBase<PartTypeInventoryReader>();
    }

}
