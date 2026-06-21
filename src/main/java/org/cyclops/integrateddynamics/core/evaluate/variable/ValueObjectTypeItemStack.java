package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ItemMatch;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.api.advancement.criterion.ValuePredicate;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNamed;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNullable;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeUniquelyNamed;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeItemStackLPElement;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeLPElementBase;

import java.util.Objects;
import java.util.Optional;

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

    public static MutableComponent getItemStackDisplayNameUsSafe(ItemStack itemStack) throws NoSuchMethodException {
        return !itemStack.isEmpty()
                ? (itemStack.getHoverName().copy().append((itemStack.getCount() > 1 ? " (" + itemStack.getCount() + ")" : "")))
                : Component.literal("");
    }

    public static MutableComponent getItemStackDisplayNameSafe(ItemStack itemStack) {
        // Certain mods may call client-side only methods,
        // so call a server-side-safe fallback method if that fails.
        try {
            return getItemStackDisplayNameUsSafe(itemStack);
        } catch (NoSuchMethodException e) {
            return Component.translatable(itemStack.getItem().getDescriptionId());
        }
    }

    @Override
    public ValueItemStack getDefault() {
        return ValueItemStack.of(ItemStack.EMPTY);
    }

    @Override
    public MutableComponent toCompactString(ValueItemStack value) {
        return ValueObjectTypeItemStack.getItemStackDisplayNameSafe(value.getRawValue());
    }

    @Override
    public void serialize(ValueOutput valueOutput, ValueItemStack value) {
        valueOutput = valueOutput.child("v");
        ItemStack itemStack;
        itemStack = value.getRawValue();
        int count = itemStack.getCount();
        if (itemStack.getCount() > 99) {
            itemStack = itemStack.copy();
            itemStack.setCount(99);
        }
        if (count > 99) {
            valueOutput.putInt("ExtendedCount", count);
        }
        valueOutput.store("stack", ItemStack.OPTIONAL_CODEC, itemStack);
    }

    @Override
    public ValueItemStack deserialize(ValueInput valueInput) {
        valueInput = valueInput.child("v").orElseThrow();
        ItemStack itemStack = valueInput.read("stack", ItemStack.OPTIONAL_CODEC).orElseThrow();
        valueInput.getInt("ExtendedCount").ifPresent(itemStack::setCount);
        return ValueItemStack.of(itemStack);
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
            public Component validate(ItemStack itemStack) {
                return null;
            }

            @Override
            public ValueObjectTypeItemStack.ValueItemStack getValue(ItemStack itemStack) {
                return ValueObjectTypeItemStack.ValueItemStack.of(itemStack);
            }

            @Override
            public ItemStack getValueAsItemStack(ValueItemStack value) {
                return value.getRawValue();
            }
        });
    }

    @Override
    public ValueItemStack materialize(ValueItemStack value) throws EvaluationException {
        return ValueItemStack.of(value.getRawValue().copy());
    }

    @Override
    public String getUniqueName(ValueItemStack value) {
        ItemStack itemStack = value.getRawValue();
        return !itemStack.isEmpty() ? BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString() : "";
    }

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
            return 37 + IModHelpers.get().getItemStackHelpers().getItemStackHashCode(itemStack);
        }

        @Override
        public String toString() {
            return "ValueItemStack(itemStack=" + this.itemStack + ")";
        }
    }

    public static class ValueItemStackPredicate extends ValuePredicate<ValueItemStack> {

        private final Optional<ItemPredicate> itemPredicate;

        public ValueItemStackPredicate(Optional<ItemPredicate> itemPredicate) {
            super(Optional.of(ValueTypes.OBJECT_ITEMSTACK), Optional.empty(), Optional.empty());
            this.itemPredicate = itemPredicate;
        }

        public Optional<ItemPredicate> getItemPredicate() {
            return itemPredicate;
        }

        @Override
        protected boolean testTyped(ValueItemStack value) {
            return super.testTyped(value) && (itemPredicate.isEmpty() || itemPredicate.get().test(value.getRawValue()));
        }
    }

}
