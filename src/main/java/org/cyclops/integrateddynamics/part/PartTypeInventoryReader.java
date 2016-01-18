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
                Aspects.Read.Inventory.BOOLEAN_FULL,
                Aspects.Read.Inventory.BOOLEAN_EMPTY,
                Aspects.Read.Inventory.BOOLEAN_NONEMPTY,
                Aspects.Read.Inventory.BOOLEAN_APPLICABLE,
                Aspects.Read.Inventory.INTEGER_COUNT,
                Aspects.Read.Inventory.LIST_ITEMSTACKS,
                Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT
        ));
    }

    @Override
    public PartStateReaderBase<PartTypeInventoryReader> constructDefaultState() {
        return new PartStateReaderBase<PartTypeInventoryReader>();
    }

}
