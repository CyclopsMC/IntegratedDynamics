package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.ToString;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ItemMatch;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
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
        super("itemstack", ValueObjectTypeItemStack.ValueItemStack.class);
    }

    public static IFormattableTextComponent getItemStackDisplayNameUsSafe(ItemStack itemStack) throws NoSuchMethodException {
        return !itemStack.isEmpty()
                ? (((IFormattableTextComponent) itemStack.getDisplayName()).appendString((itemStack.getCount() > 1 ? " (" + itemStack.getCount() + ")" : "")))
                : new StringTextComponent("");
    }

    public static IFormattableTextComponent getItemStackDisplayNameSafe(ItemStack itemStack) {
        // Certain mods may call client-side only methods,
        // so call a server-side-safe fallback method if that fails.
        try {
            return getItemStackDisplayNameUsSafe(itemStack);
        } catch (NoSuchMethodException e) {
            return new TranslationTextComponent(itemStack.getTranslationKey());
        }
    }

    @Override
    public ValueItemStack getDefault() {
        return ValueItemStack.of(ItemStack.EMPTY);
    }

    @Override
    public IFormattableTextComponent toCompactString(ValueItemStack value) {
        return ValueObjectTypeItemStack.getItemStackDisplayNameSafe(value.getRawValue());
    }

    @Override
    public INBT serialize(ValueItemStack value) {
        CompoundNBT tag = new CompoundNBT();
        ItemStack itemStack = value.getRawValue();
        if(!itemStack.isEmpty()) {
            itemStack.write(tag);
            tag.putInt("Count", itemStack.getCount());
        }
        return tag;
    }

    @Override
    public ValueItemStack deserialize(INBT value) {
        if (value instanceof CompoundNBT) {
            CompoundNBT tag = (CompoundNBT) value;
            // Forge returns air for tags with negative count,
            // so we set it to 1 for deserialization and fix it afterwards.
            int realCount = tag.getInt("Count");
            // Consider the tag immutable, to avoid changes elsewhere
            tag = tag.copy();
            tag.putByte("Count", (byte)1);
            ItemStack itemStack = ItemStack.read(tag);
            if (!itemStack.isEmpty()) {
                itemStack.setCount(realCount);
            }
            return ValueItemStack.of(itemStack);
        } else {
            return ValueItemStack.of(ItemStack.EMPTY);
        }
    }

    @Override
    public String getName(ValueItemStack a) {
        return toCompactString(a).getString();
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
            public ITextComponent validate(ItemStack itemStack) {
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
        return !itemStack.isEmpty() ? itemStack.getItem().getRegistryName().toString() : "";
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
            return o instanceof ValueItemStack && ItemMatch.areItemStacksEqual(((ValueItemStack) o).itemStack, this.itemStack, ItemMatch.EXACT);
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
