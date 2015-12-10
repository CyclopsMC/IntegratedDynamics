package org.cyclops.integrateddynamics.part.aspect.read.inventory;

import com.google.common.collect.Sets;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectProperties;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadObjectItemStackBase;

/**
 * Aspect that gets the itemstack in the active slot
 * @author rubensworks
 */
public class AspectReadObjectItemStackInventorySlot extends AspectReadObjectItemStackBase {

    public static final AspectPropertyTypeInstance<ValueTypeInteger, ValueTypeInteger.ValueInteger> PROP_SLOTID =
            new AspectPropertyTypeInstance<>(ValueTypes.INTEGER, "aspect.aspecttypes.integrateddynamics.integer.slotid.name");

    @Override
    protected String getUnlocalizedItemStackType() {
        return "inventory";
    }

    @Override
    protected ValueObjectTypeItemStack.ValueItemStack getValue(PartTarget target, AspectProperties properties) {
        DimPos dimPos = target.getTarget().getPos();
        TileEntity tile = dimPos.getWorld().getTileEntity(dimPos.getBlockPos());
        ItemStack value = null;
        if(tile instanceof IInventory) {
            value = countInventoryItems((IInventory) tile, properties.getValue(PROP_SLOTID).getRawValue());
        }
        return ValueObjectTypeItemStack.ValueItemStack.of(value);
    }

    protected static ItemStack countInventoryItems(IInventory inventory, int slotId) {
        if(slotId >= 0 && slotId < inventory.getSizeInventory()) {
            return inventory.getStackInSlot(slotId);
        }
        return null;
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
