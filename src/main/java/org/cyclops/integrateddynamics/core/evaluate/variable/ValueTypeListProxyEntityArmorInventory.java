package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Iterables;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;

/**
 * A list proxy for the inventory of an entity.
 */
public class ValueTypeListProxyEntityArmorInventory extends ValueTypeListProxyEntityBase<ValueObjectTypeItemStack, ValueObjectTypeItemStack.ValueItemStack> implements INBTProvider {

    public ValueTypeListProxyEntityArmorInventory(Level world, Entity entity) {
        super(ValueTypeListProxyFactories.ENTITY_ARMORINVENTORY.getName(), ValueTypes.OBJECT_ITEMSTACK, world, entity);
    }

    public ValueTypeListProxyEntityArmorInventory() {
        this(null, null);
    }

    protected ItemStack[] getInventory() {
        Entity e = getEntity();
        if(e instanceof LivingEntity livingEntity) {
            ItemStack[] inventory = Iterables.toArray(livingEntity.getArmorSlots(), ItemStack.class);
            if(inventory != null) {
                return inventory;
            }
        }
        return new ItemStack[0];
    }

    @Override
    public int getLength() {
        return getInventory().length;
    }

    @Override
    public ValueObjectTypeItemStack.ValueItemStack get(int index) {
        return ValueObjectTypeItemStack.ValueItemStack.of(getInventory()[index]);
    }
}
