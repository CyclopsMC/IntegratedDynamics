package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;

/**
 * A list proxy for an inventory at a certain position.
 */
public class ValueTypeListProxyPositionedInventory extends ValueTypeListProxyBase<ValueObjectTypeItemStack, ValueObjectTypeItemStack.ValueItemStack> implements INBTProvider {

    @NBTPersist
    private DimPos pos;

    public ValueTypeListProxyPositionedInventory() {
        this(null);
    }

    public ValueTypeListProxyPositionedInventory(DimPos pos) {
        super(ValueTypeListProxyFactories.POSITIONED_INVENTORY.getName(), ValueTypes.OBJECT_ITEMSTACK);
        this.pos = pos;
    }

    protected IInventory getInventory() {
        return TileHelpers.getSafeTile(pos.getWorld(), pos.getBlockPos(), IInventory.class);
    }

    @Override
    public int getLength() {
        return getInventory().getSizeInventory();
    }

    @Override
    public ValueObjectTypeItemStack.ValueItemStack get(int index) {
        return ValueObjectTypeItemStack.ValueItemStack.of(getInventory().getStackInSlot(index));
    }

    @Override
    public void writeGeneratedFieldsToNBT(NBTTagCompound tag) {

    }

    @Override
    public void readGeneratedFieldsFromNBT(NBTTagCompound tag) {

    }
}
