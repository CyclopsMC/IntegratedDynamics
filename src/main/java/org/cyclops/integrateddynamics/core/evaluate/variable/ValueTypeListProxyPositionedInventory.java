package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Iterators;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.IModHelpersNeoForge;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Optional;

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

    protected Optional<ResourceHandler<ItemResource>> getInventory() {
        return IModHelpersNeoForge.get().getCapabilityHelpers().getCapability(getPos(), getSide(), net.neoforged.neoforge.capabilities.Capabilities.Item.BLOCK);
    }

    @Override
    public int getLength() {
        return getInventory()
                .map(ResourceHandler<ItemResource>::size)
                .orElse(0);
    }

    @Override
    public ValueObjectTypeItemStack.ValueItemStack get(int index) {
        return ValueObjectTypeItemStack.ValueItemStack.of(getInventory()
                .map(itemHandler -> itemHandler.getResource(index).toStack(itemHandler.getAmountAsInt(index)))
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

        @Nullable
        private final ResourceHandler<ItemResource> itemHandler;
        private int index = 0;

        public ListFactoryIterator(@Nullable ResourceHandler<ItemResource> itemHandler) {
            this.itemHandler = itemHandler;
        }

        @Override
        public boolean hasNext() {
            return itemHandler != null && index < itemHandler.size();
        }

        @Override
        public ValueObjectTypeItemStack.ValueItemStack next() {
            return ValueObjectTypeItemStack.ValueItemStack.of(this.itemHandler == null ? ItemStack.EMPTY : this.itemHandler.getResource(index).toStack(itemHandler.getAmountAsInt(index++)));
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }
}
