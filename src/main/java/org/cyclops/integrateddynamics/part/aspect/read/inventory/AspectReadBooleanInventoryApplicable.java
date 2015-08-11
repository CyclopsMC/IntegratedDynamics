package org.cyclops.integrateddynamics.part.aspect.read.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.part.PartTarget;

/**
 * Aspect that checks if the target is an inventory.
 * @author rubensworks
 */
public class AspectReadBooleanInventoryApplicable extends AspectReadBooleanInventoryBase {

    @Override
    protected String getUnlocalizedBooleanRedstoneType() {
        return "applicable";
    }

    @Override
    protected ValueTypeBoolean.ValueBoolean getValue(PartTarget target) {
        DimPos dimPos = target.getTarget().getPos();
        TileEntity tile = dimPos.getWorld().getTileEntity(dimPos.getBlockPos());
        return ValueTypeBoolean.ValueBoolean.of(tile instanceof IInventory);
    }
}
