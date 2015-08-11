package org.cyclops.integrateddynamics.part.aspect.read;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.part.PartTarget;

/**
 * Aspect that can count the amount of items in an inventory
 * @author rubensworks
 */
public class AspectReadIntegerInventory extends AspectReadIntegerBase {

    @Override
    protected String getUnlocalizedIntegerType() {
        return "inventory";
    }

    @Override
    protected ValueTypeInteger.ValueInteger getValue(PartTarget target) {
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
