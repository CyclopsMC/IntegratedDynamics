package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;

/**
 * A list proxy for the inventory of an entity.
 */
public class ValueTypeListProxyEntityInventory extends ValueTypeListProxyEntityBase<ValueObjectTypeItemStack, ValueObjectTypeItemStack.ValueItemStack> implements INBTProvider {

    public ValueTypeListProxyEntityInventory(World world, Entity entity) {
        super(ValueTypeListProxyFactories.ENTITY_INVENTORY.getName(), ValueTypes.OBJECT_ITEMSTACK, world, entity);
    }

    protected NonNullList<ItemStack> getInventory() {
        Entity e = getEntity();
        if(e != null && e instanceof EntityPlayer) {
            return ((EntityPlayer) e).inventory.mainInventory;
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

    @Override
    public void writeGeneratedFieldsToNBT(NBTTagCompound tag) {

    }

    @Override
    public void readGeneratedFieldsFromNBT(NBTTagCompound tag) {

    }
}
