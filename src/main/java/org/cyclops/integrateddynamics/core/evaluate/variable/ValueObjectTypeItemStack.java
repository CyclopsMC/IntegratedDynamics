package org.cyclops.integrateddynamics.core.evaluate.variable;

import lombok.ToString;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ItemMatch;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
import org.cyclops.integrateddynamics.api.advancement.criterion.ValuePredicate;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNamed;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNullable;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeUniquelyNamed;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
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
                ? (((MutableComponent) itemStack.getHoverName()).append((itemStack.getCount() > 1 ? " (" + itemStack.getCount() + ")" : "")))
                : Component.literal("");
    }

    public static MutableComponent getItemStackDisplayNameSafe(ItemStack itemStack) {
        // Certain mods may call client-side only methods,
        // so call a server-side-safe fallback method if that fails.
        try {
            return getItemStackDisplayNameUsSafe(itemStack);
        } catch (NoSuchMethodException e) {
            return Component.translatable(itemStack.getDescriptionId());
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
    public Tag serialize(ValueItemStack value) {
        CompoundTag tag = new CompoundTag();
        ItemStack itemStack = value.getRawValue();
        if(!itemStack.isEmpty()) {
            itemStack.save(tag);
            tag.putInt("Count", itemStack.getCount());
        }
        return tag;
    }

    @Override
    public ValueItemStack deserialize(ValueDeseralizationContext valueDeseralizationContext, Tag value) {
        if (value instanceof CompoundTag) {
            CompoundTag tag = (CompoundTag) value;
            // Forge returns air for tags with negative count,
            // so we set it to 1 for deserialization and fix it afterwards.
            int realCount = tag.getInt("Count");
            // Consider the tag immutable, to avoid changes elsewhere
            tag = tag.copy();
            tag.putByte("Count", (byte)1);
            ItemStack itemStack = ItemStack.of(tag);
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
            public Component validate(ItemStack itemStack) {
                return null;
            }

            @Override
            public ValueObjectTypeItemStack.ValueItemStack getValue(ItemStack itemStack) {
                return ValueObjectTypeItemStack.ValueItemStack.of(itemStack);
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
            return super.testTyped(value) && (itemPredicate.isEmpty() || itemPredicate.get().matches(value.getRawValue()));
        }
    }

}
