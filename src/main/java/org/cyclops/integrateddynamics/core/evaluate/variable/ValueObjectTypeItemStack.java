package org.cyclops.integrateddynamics.core.evaluate.variable;

import lombok.ToString;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNamed;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNullable;

import java.util.Objects;

/**
 * Value type with values that are itemstacks.
 * @author rubensworks
 */
public class ValueObjectTypeItemStack extends ValueObjectTypeBase<ValueObjectTypeItemStack.ValueItemStack> implements
        IValueTypeNamed<ValueObjectTypeItemStack.ValueItemStack>, IValueTypeNullable<ValueObjectTypeItemStack.ValueItemStack> {

    public ValueObjectTypeItemStack() {
        super("itemstack");
    }

    @Override
    public ValueItemStack getDefault() {
        return ValueItemStack.of(ItemStack.EMPTY);
    }

    @Override
    public String toCompactString(ValueItemStack value) {
        ItemStack itemStack = value.getRawValue();
        return !itemStack.isEmpty() ? itemStack.getDisplayName() : "";
    }

    @Override
    public String serialize(ValueItemStack value) {
        NBTTagCompound tag = new NBTTagCompound();
        ItemStack itemStack = value.getRawValue();
        if(!itemStack.isEmpty()) {
            itemStack.writeToNBT(tag);
            tag.setInteger("Count", itemStack.getCount());
        }
        return tag.toString();
    }

    @Override
    public ValueItemStack deserialize(String value) {
        try {
            NBTTagCompound tag = JsonToNBT.getTagFromJson(value);
            ItemStack itemStack = new ItemStack(tag);
            if (!itemStack.isEmpty()) {
                itemStack.setCount(tag.getInteger("Count"));
            }
            return ValueItemStack.of(itemStack);
        } catch (NBTException e) {
            return ValueItemStack.of(ItemStack.EMPTY);
        }
    }

    @Override
    public String getName(ValueItemStack a) {
        return toCompactString(a);
    }

    @Override
    public boolean isNull(ValueItemStack a) {
        return !a.getRawValue().isEmpty();
    }

    @ToString
    public static class ValueItemStack extends ValueBase {

        private final ItemStack itemStack;

        private ValueItemStack(ItemStack itemStack) {
            super(ValueTypes.OBJECT_ITEMSTACK);
            this.itemStack = Objects.requireNonNull(itemStack, "Attempted to create a ValueItemStack for a null ItemStack.");
        }

        public static ValueItemStack of(ItemStack itemStack) {
            return new ValueItemStack(itemStack);
        }

        public ItemStack getRawValue() {
            return itemStack;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof ValueItemStack && ItemStackHelpers.areItemStacksIdentical(((ValueItemStack) o).itemStack, this.itemStack);
        }
    }

}
