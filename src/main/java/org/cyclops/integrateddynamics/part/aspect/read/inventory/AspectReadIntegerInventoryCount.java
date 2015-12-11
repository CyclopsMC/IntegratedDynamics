package org.cyclops.integrateddynamics.part.aspect.read.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;

/**
 * Aspect that can count the amount of items in an inventory
 * @author rubensworks
 */
public class AspectReadIntegerInventoryCount extends AspectReadIntegerInventoryBase {

    @Override
    protected String getUnlocalizedIntegerWorldType() {
        return "count";
    }

    @Override
    protected ValueTypeInteger.ValueInteger getValue(PartTarget target, IAspectProperties properties) {
        DimPos dimPos = target.getTarget().getPos();
        TileEntity tile = dimPos.getWorld().getTileEntity(dimPos.getBlockPos());
        int value = 0;
        if(tile instanceof IInventory) {
            value = countInventoryItems((IInventory) tile);
        }
        return ValueTypeInteger.ValueInteger.of(value);
    }

    protected static int countInventoryItems(IInventory inventory) {
        int count = 0;
        for(int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack itemStack = inventory.getStackInSlot(i);
            if(itemStack != null) {
                count += itemStack.stackSize;
            }
        }
        return count;
    }

}
