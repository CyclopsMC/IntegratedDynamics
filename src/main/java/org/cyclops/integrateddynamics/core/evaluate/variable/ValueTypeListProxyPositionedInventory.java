package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;

/**
 * A list proxy for an inventory at a certain position.
 */
public class ValueTypeListProxyPositionedInventory extends ValueTypeListProxyPositioned<ValueObjectTypeItemStack, ValueObjectTypeItemStack.ValueItemStack> implements INBTProvider {

    public ValueTypeListProxyPositionedInventory(DimPos pos, EnumFacing side) {
        super(ValueTypeListProxyFactories.POSITIONED_INVENTORY.getName(), ValueTypes.OBJECT_ITEMSTACK, pos, side);
    }

    protected IItemHandler getInventory() {
        return TileHelpers.getCapability(getPos(), getSide(), CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
    }

    @Override
    public int getLength() {
        IItemHandler inventory = getInventory();
        if(inventory == null) {
            return 0;
        }
        return inventory.getSlots();
    }

    @Override
    public ValueObjectTypeItemStack.ValueItemStack get(int index) {
        return ValueObjectTypeItemStack.ValueItemStack.of(getInventory().getStackInSlot(index));
    }
}
