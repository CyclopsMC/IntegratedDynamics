package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.base.Optional;
import lombok.ToString;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNamed;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNullable;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeItemStackLPElement;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeLPElementBase;

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
        return ValueItemStack.of(null);
    }

    @Override
    public String toCompactString(ValueItemStack value) {
        Optional<ItemStack> itemStack = value.getRawValue();
        return itemStack.isPresent() ? itemStack.get().getDisplayName() : "";
    }

    @Override
    public String serialize(ValueItemStack value) {
        NBTTagCompound tag = new NBTTagCompound();
        Optional<ItemStack> itemStack = value.getRawValue();
        if(itemStack.isPresent()) {
            itemStack.get().writeToNBT(tag);
            tag.setInteger("Count", itemStack.get().stackSize);
        }
        return tag.toString();
    }

    @Override
    public ValueItemStack deserialize(String value) {
        try {
            NBTTagCompound tag = JsonToNBT.getTagFromJson(value);
            ItemStack itemStack = ItemStack.loadItemStackFromNBT(tag);
            if (itemStack != null) {
                itemStack.stackSize = tag.getInteger("Count");
            }
            return ValueItemStack.of(itemStack);
        } catch (NBTException e) {
            return null;
        }
    }

    @Override
    public String getName(ValueItemStack a) {
        return toCompactString(a);
    }

    @Override
    public boolean isNull(ValueItemStack a) {
        return !a.getRawValue().isPresent();
    }

    @Override
    public ValueTypeLPElementBase createLogicProgrammerElement() {
        return new ValueTypeItemStackLPElement<>(this, new ValueTypeItemStackLPElement.IItemStackToValue<ValueObjectTypeItemStack.ValueItemStack>() {
            @Override
            public boolean isNullable() {
                return true;
            }

            @Override
            public L10NHelpers.UnlocalizedString validate(ItemStack itemStack) {
                return null;
            }

            @Override
            public ValueObjectTypeItemStack.ValueItemStack getValue(ItemStack itemStack) {
                return ValueObjectTypeItemStack.ValueItemStack.of(itemStack);
            }
        });
    }

    @ToString
    public static class ValueItemStack extends ValueOptionalBase<ItemStack> {

        private ValueItemStack(ItemStack itemStack) {
            super(ValueTypes.OBJECT_ITEMSTACK, itemStack);
        }

        public static ValueItemStack of(ItemStack itemStack) {
            return new ValueItemStack(itemStack);
        }

        @Override
        protected boolean isEqual(ItemStack a, ItemStack b) {
            return ItemStackHelpers.areItemStacksIdentical(a, b);
        }
    }

}
