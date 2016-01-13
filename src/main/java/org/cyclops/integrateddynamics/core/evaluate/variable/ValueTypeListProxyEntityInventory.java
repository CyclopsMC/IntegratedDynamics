package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;

/**
 * A list proxy for the inventory of an entity.
 */
public class ValueTypeListProxyEntityInventory extends ValueTypeListProxyEntityBase<ValueObjectTypeItemStack, ValueObjectTypeItemStack.ValueItemStack> implements INBTProvider {

    public ValueTypeListProxyEntityInventory() {
        this(null, null);
    }

    public ValueTypeListProxyEntityInventory(World world, Entity entity) {
        super(ValueTypeListProxyFactories.ENTITY_INVENTORY.getName(), ValueTypes.OBJECT_ITEMSTACK, world, entity);
    }

    protected ItemStack[] getInventory() {
        Entity e = getEntity();
        if(e != null && e instanceof EntityPlayer) {
            return ((EntityPlayer) e).inventory.mainInventory;
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

    @Override
    public void writeGeneratedFieldsToNBT(NBTTagCompound tag) {

    }

    @Override
    public void readGeneratedFieldsFromNBT(NBTTagCompound tag) {

    }
}
