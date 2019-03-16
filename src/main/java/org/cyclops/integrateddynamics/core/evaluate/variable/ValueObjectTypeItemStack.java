package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.ToString;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.advancement.criterion.ValuePredicate;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNamed;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNullable;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeUniquelyNamed;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeItemStackLPElement;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeLPElementBase;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Value type with values that are itemstacks.
 * @author rubensworks
 */
public class ValueObjectTypeItemStack extends ValueObjectTypeBase<ValueObjectTypeItemStack.ValueItemStack> implements
        IValueTypeNamed<ValueObjectTypeItemStack.ValueItemStack>,
        IValueTypeUniquelyNamed<ValueObjectTypeItemStack.ValueItemStack>,
        IValueTypeNullable<ValueObjectTypeItemStack.ValueItemStack> {

    public ValueObjectTypeItemStack() {
        super("itemstack");
    }

    public static String getItemStackDisplayNameUsSafe(ItemStack itemStack) throws NoSuchMethodException {
        return !itemStack.isEmpty() ? (itemStack.getDisplayName() + (itemStack.getCount() > 1 ? " (" + itemStack.getCount() + ")" : "")) : "";
    }

    public static String getItemStackDisplayNameSafe(ItemStack itemStack) {
        // Certain mods may call client-side only methods,
        // so call a server-side-safe fallback method if that fails.
        try {
            return getItemStackDisplayNameUsSafe(itemStack);
        } catch (NoSuchMethodException e) {
            return L10NHelpers.localize(itemStack.getTranslationKey() + ".name");
        }
    }

    @Override
    public ValueItemStack getDefault() {
        return ValueItemStack.of(ItemStack.EMPTY);
    }

    @Override
    public String toCompactString(ValueItemStack value) {
        return ValueObjectTypeItemStack.getItemStackDisplayNameSafe(value.getRawValue());
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
            // Forge returns air for tags with negative count,
            // so we set it to 1 for deserialization and fix it afterwards.
            int realCount = tag.getInteger("Count");
            tag.setByte("Count", (byte)1);
            ItemStack itemStack = new ItemStack(tag);
            if (!itemStack.isEmpty()) {
                itemStack.setCount(realCount);
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
        return a.getRawValue().isEmpty();
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

    @Override
    public ValuePredicate<ValueItemStack> deserializeValuePredicate(JsonObject element, @Nullable IValue value) {
        JsonElement jsonElement = element.get("value");
        ItemPredicate itemPredicate = null;
        if (jsonElement != null && !jsonElement.isJsonNull()) {
            itemPredicate = ItemPredicate.deserialize(element.get("value"));
        }
        return new ValueItemStackPredicate(this, value, itemPredicate);
    }

    @Override
    public ValueItemStack materialize(ValueItemStack value) throws EvaluationException {
        return ValueItemStack.of(value.getRawValue().copy());
    }

    @Override
    public String getUniqueName(ValueItemStack value) {
        ItemStack itemStack = value.getRawValue();
        return !itemStack.isEmpty() ? itemStack.getItem().getRegistryName()
                + (itemStack.getMetadata() > 0 ? " " + itemStack.getMetadata() : "") : "";
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
            return o instanceof ValueItemStack && ItemStack.areItemStacksEqual(((ValueItemStack) o).itemStack, this.itemStack);
        }

        @Override
        public int hashCode() {
            return 37 + ItemStackHelpers.getItemStackHashCode(itemStack);
        }
    }

    public static class ValueItemStackPredicate extends ValuePredicate<ValueItemStack> {

        private final @Nullable ItemPredicate itemPredicate;

        public ValueItemStackPredicate(@Nullable IValueType valueType, @Nullable IValue value, @Nullable ItemPredicate itemPredicate) {
            super(valueType, value);
            this.itemPredicate = itemPredicate;
        }

        @Override
        protected boolean testTyped(ValueItemStack value) {
            return super.testTyped(value) && (itemPredicate == null || itemPredicate.test(value.getRawValue()));
        }
    }

}
