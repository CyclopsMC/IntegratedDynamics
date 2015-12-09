package org.cyclops.integrateddynamics.core.evaluate.variable;

import lombok.ToString;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;

/**
 * Value type with values that are itemstacks.
 * @author rubensworks
 */
public class ValueObjectTypeItemStack extends ValueObjectTypeBase<ValueObjectTypeItemStack.ValueItemStack> implements IValueTypeNamed<ValueObjectTypeItemStack.ValueItemStack> {

    public ValueObjectTypeItemStack() {
        super("itemstack");
    }

    @Override
    public ValueItemStack getDefault() {
        return ValueItemStack.of(null);
    }

    @Override
    public String toCompactString(ValueItemStack value) {
        ItemStack itemStack = value.getRawValue();
        if(itemStack == null) return "";
        return itemStack.getDisplayName();
    }

    @Override
    public String serialize(ValueItemStack value) {
        NBTTagCompound tag = new NBTTagCompound();
        ItemStack itemStack = value.getRawValue();
        if(itemStack != null) value.getRawValue().writeToNBT(tag);
        return tag.toString();
    }

    @Override
    public ValueItemStack deserialize(String value) {
        try {
            NBTTagCompound tag = JsonToNBT.getTagFromJson(value);
            ItemStack itemStack = ItemStack.loadItemStackFromNBT(tag);
            return ValueItemStack.of(itemStack);
        } catch (NBTException e) {
            return null;
        }
    }

    @Override
    public String getName(ValueItemStack a) {
        ItemStack itemStack = a.getRawValue();
        if(itemStack == null) return "";
        return itemStack.getDisplayName();
    }

    @ToString
    public static class ValueItemStack extends ValueBase {

        private final ItemStack itemStack;

        private ValueItemStack(ItemStack itemStack) {
            super(ValueTypes.OBJECT_ITEMSTACK);
            this.itemStack = itemStack;
        }

        public static ValueItemStack of(ItemStack itemStack) {
            return new ValueItemStack(itemStack);
        }

        public ItemStack getRawValue() {
            return itemStack;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof ValueItemStack && ItemStackHelpers.areItemStacksIdentical(((ValueItemStack) o).itemStack, itemStack);
        }
    }

}
