package org.cyclops.integrateddynamics.part.aspect.read.inventory;

import com.google.common.collect.Sets;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectProperties;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectPropertyTypeInstance;

/**
 * Aspect that can count the stacksize in the active slot
 * @author rubensworks
 */
public class AspectReadIntegerInventoryCountSlot extends AspectReadIntegerInventoryBase {

    public static final AspectPropertyTypeInstance<ValueTypeInteger, ValueTypeInteger.ValueInteger> PROP_SLOTID =
            new AspectPropertyTypeInstance<>(ValueTypes.INTEGER, "aspect.aspecttypes.integrateddynamics.integer.slotid.name");

    @Override
    protected String getUnlocalizedIntegerWorldType() {
        return "countslot";
    }

    @Override
    protected ValueTypeInteger.ValueInteger getValue(PartTarget target, AspectProperties properties) {
        DimPos dimPos = target.getTarget().getPos();
        TileEntity tile = dimPos.getWorld().getTileEntity(dimPos.getBlockPos());
        int value = 0;
        if(tile instanceof IInventory) {
            value = countInventoryItems((IInventory) tile, properties.getValue(PROP_SLOTID).getRawValue());
        }
        return ValueTypeInteger.ValueInteger.of(value);
    }

    protected static int countInventoryItems(IInventory inventory, int slotId) {
        if(slotId >= 0 && slotId < inventory.getSizeInventory()) {
            ItemStack itemStack = inventory.getStackInSlot(slotId);
            return itemStack == null ? 0 : itemStack.stackSize;
        }
        return 0;
    }

    @Override
    protected AspectProperties createDefaultProperties() {
        AspectProperties properties = new AspectProperties(Sets.<AspectPropertyTypeInstance>newHashSet(
                PROP_SLOTID
        ));
        properties.setValue(PROP_SLOTID, ValueTypeInteger.ValueInteger.of(0)); // Not required in this case, but we do this here just as an example on how to set default values.
        return properties;
    }

}
