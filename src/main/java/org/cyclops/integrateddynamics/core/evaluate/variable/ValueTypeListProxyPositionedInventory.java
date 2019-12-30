package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Iterators;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;

import java.util.Iterator;

/**
 * A list proxy for an inventory at a certain position.
 */
public class ValueTypeListProxyPositionedInventory extends ValueTypeListProxyPositioned<ValueObjectTypeItemStack, ValueObjectTypeItemStack.ValueItemStack> implements INBTProvider {

    public ValueTypeListProxyPositionedInventory(DimPos pos, Direction side) {
        super(ValueTypeListProxyFactories.POSITIONED_INVENTORY.getName(), ValueTypes.OBJECT_ITEMSTACK, pos, side);
    }

    public ValueTypeListProxyPositionedInventory() {
        this(null, null);
    }

    protected LazyOptional<IItemHandler> getInventory() {
        return TileHelpers.getCapability(getPos(), getSide(), CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
    }

    @Override
    public int getLength() {
        return getInventory()
                .map(IItemHandler::getSlots)
                .orElse(0);
    }

    @Override
    public ValueObjectTypeItemStack.ValueItemStack get(int index) {
        return ValueObjectTypeItemStack.ValueItemStack.of(getInventory()
                .map(itemHandler -> itemHandler.getStackInSlot(index))
                .orElse(ItemStack.EMPTY));
    }

    @Override
    public Iterator<ValueObjectTypeItemStack.ValueItemStack> iterator() {
        // We use a custom iterator that retrieves the itemhandler capability only once.
        // Because for large inventories, the capability would have to be retrieved for every single slot,
        // which could result in a major performance problem.
        return getInventory()
                .map(itemHandler -> (Iterator<ValueObjectTypeItemStack.ValueItemStack>) new ValueTypeListProxyPositionedInventory.ListFactoryIterator(itemHandler))
                .orElse(Iterators.forArray());
    }

    public static class ListFactoryIterator implements Iterator<ValueObjectTypeItemStack.ValueItemStack> {

        private final IItemHandler itemHandler;
        private int index = 0;

        public ListFactoryIterator(IItemHandler itemHandler) {
            this.itemHandler = itemHandler;
        }

        @Override
        public boolean hasNext() {
            return itemHandler != null && index < itemHandler.getSlots();
        }

        @Override
        public ValueObjectTypeItemStack.ValueItemStack next() {
            return ValueObjectTypeItemStack.ValueItemStack.of(this.itemHandler.getStackInSlot(index++));
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }
}
