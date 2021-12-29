package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;

/**
 * A list proxy for the inventory of an entity.
 */
public class ValueTypeListProxyEntityInventory extends ValueTypeListProxyEntityBase<ValueObjectTypeItemStack, ValueObjectTypeItemStack.ValueItemStack> implements INBTProvider {

    public ValueTypeListProxyEntityInventory(Level world, Entity entity) {
        super(ValueTypeListProxyFactories.ENTITY_INVENTORY.getName(), ValueTypes.OBJECT_ITEMSTACK, world, entity);
    }

    public ValueTypeListProxyEntityInventory() {
        this(null, null);
    }

    protected NonNullList<ItemStack> getInventory() {
        Entity e = getEntity();
        if(e != null && e instanceof Player) {
            return ((Player) e).getInventory().items;
        }
        return NonNullList.create();
    }

    @Override
    public int getLength() {
        return getInventory().size();
    }

    @Override
    public ValueObjectTypeItemStack.ValueItemStack get(int index) {
        return ValueObjectTypeItemStack.ValueItemStack.of(getInventory().get(index));
    }
}
