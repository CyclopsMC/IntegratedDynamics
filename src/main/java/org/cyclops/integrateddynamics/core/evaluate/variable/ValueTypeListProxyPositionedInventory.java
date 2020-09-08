package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;

import javax.annotation.Nullable;
import java.util.Iterator;

/**
 * A list proxy for an inventory at a certain position.
 */
public class ValueTypeListProxyPositionedInventory extends ValueTypeListProxyPositioned<ValueObjectTypeItemStack, ValueObjectTypeItemStack.ValueItemStack> implements INBTProvider {

    public ValueTypeListProxyPositionedInventory(DimPos pos, EnumFacing side) {
        super(ValueTypeListProxyFactories.POSITIONED_INVENTORY.getName(), ValueTypes.OBJECT_ITEMSTACK, pos, side);
    }

    public ValueTypeListProxyPositionedInventory() {
        this(null, null);
    }

    @Nullable
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
        IItemHandler inventory = getInventory();
        return ValueObjectTypeItemStack.ValueItemStack.of(inventory == null ? ItemStack.EMPTY : inventory.getStackInSlot(index));
    }

    @Override
    public Iterator<ValueObjectTypeItemStack.ValueItemStack> iterator() {
        // We use a custom iterator that retrieves the itemhandler capability only once.
        // Because for large inventories, the capability would have to be retrieved for every single slot,
        // which could result in a major performance problem.
        return new ValueTypeListProxyPositionedInventory.ListFactoryIterator(getInventory());
    }

    public static class ListFactoryIterator implements Iterator<ValueObjectTypeItemStack.ValueItemStack> {

        @Nullable
        private final IItemHandler itemHandler;
        private int index = 0;

        public ListFactoryIterator(@Nullable IItemHandler itemHandler) {
            this.itemHandler = itemHandler;
        }

        @Override
        public boolean hasNext() {
            return itemHandler != null && index < itemHandler.getSlots();
        }

        @Override
        public ValueObjectTypeItemStack.ValueItemStack next() {
            return ValueObjectTypeItemStack.ValueItemStack.of(this.itemHandler == null ? ItemStack.EMPTY : this.itemHandler.getStackInSlot(index++));
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }
}
