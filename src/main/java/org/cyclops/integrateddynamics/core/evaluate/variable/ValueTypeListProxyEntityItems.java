package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;

import javax.annotation.Nullable;

/**
 * A list proxy for the item handler items of an entity.
 */
public class ValueTypeListProxyEntityItems extends ValueTypeListProxyEntityCapability<IItemHandler, ValueObjectTypeItemStack, ValueObjectTypeItemStack.ValueItemStack> implements INBTProvider {

    public ValueTypeListProxyEntityItems(World world, Entity entity, @Nullable EnumFacing side) {
        super(ValueTypeListProxyFactories.ENTITY_CAPABILITY_ITEMS.getName(), ValueTypes.OBJECT_ITEMSTACK,
                world, entity, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
    }

    public ValueTypeListProxyEntityItems() {
        this(null, null, null);
    }

    @Override
    public int getLength() {
        return getCapability().map(handler -> handler.getSlots()).orElse(0);
    }

    @Override
    public ValueObjectTypeItemStack.ValueItemStack get(int index) {
        return ValueObjectTypeItemStack.ValueItemStack.of(getCapability().map(handler -> handler.getStackInSlot(index)).orElse(ItemStack.EMPTY));
    }
}
