package org.cyclops.integrateddynamics.part.aspect.read.inventory;

import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeListProxyPositionedInventory;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadListBase;

/**
 * Base class for integer inventory aspects.
 * @author rubensworks
 */
public class AspectReadListInventoryItemStacks extends AspectReadListBase {

    @Override
    protected String getUnlocalizedListType() {
        return "inventory.itemstacks";
    }

    @Override
    protected ValueTypeList.ValueList getValue(PartTarget target, IAspectProperties properties) {
        return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyPositionedInventory(target.getTarget().getPos()));
    }
}
