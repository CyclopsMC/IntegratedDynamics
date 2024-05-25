package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;

import javax.annotation.Nullable;

/**
 * A list proxy for the item handler items of an entity.
 */
public class ValueTypeListProxyEntityItems extends ValueTypeListProxyEntityCapability<IItemHandler, Void, ValueObjectTypeItemStack, ValueObjectTypeItemStack.ValueItemStack> implements INBTProvider {

    public ValueTypeListProxyEntityItems(Level world, Entity entity, @Nullable Direction side) {
        super(ValueTypeListProxyFactories.ENTITY_CAPABILITY_ITEMS.getName(), ValueTypes.OBJECT_ITEMSTACK,
                world, entity, Capabilities.ItemHandler.ENTITY, side);
    }

    public ValueTypeListProxyEntityItems() {
        this(null, null, null);
    }

    @Override
    public int getLength() {
        return getCapability().map(IItemHandler::getSlots).orElse(0);
    }

    @Override
    public ValueObjectTypeItemStack.ValueItemStack get(int index) {
        return ValueObjectTypeItemStack.ValueItemStack.of(getCapability().map(handler -> handler.getStackInSlot(index)).orElse(ItemStack.EMPTY));
    }
}
