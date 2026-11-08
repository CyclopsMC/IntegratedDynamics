package org.cyclops.integrateddynamics.core.evaluate.expression.tree;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * The tags of values that are written with their type in the text of expressions, as in {@code @Fluid{amount: 1000, id: "water"}}.
 * Value types write their value in a field "v", which the text leaves out, so values are written as in commands.
 * Lists, which write more than that field, are written as a whole, and items as their stack, with a count above 99 in it.
 * @author rubensworks
 */
public final class ExpressionValueTags {

    private static final String KEY_VALUE = "v";
    private static final String KEY_STACK = "stack";
    private static final String KEY_EXTENDED_COUNT = "ExtendedCount";

    private ExpressionValueTags() {
    }

    /**
     * @param context For serializing the value.
     * @param value A value.
     * @return Its tag in the text.
     */
    public static Tag toText(ValueDeseralizationContext context, IValue value) {
        CompoundTag tag = ExpressionTrees.serializeValue(context, value);
        // Lists, and absent values, such as no block, which write no field
        if (tag.size() != 1 || !tag.contains(KEY_VALUE)) {
            return tag;
        }
        Tag inner = tag.get(KEY_VALUE);
        if (value.getType() == ValueTypes.OBJECT_ITEMSTACK && inner instanceof CompoundTag item) {
            CompoundTag stack = item.getCompoundOrEmpty(KEY_STACK).copy();
            item.getInt(KEY_EXTENDED_COUNT).ifPresent(count -> stack.putInt(KEY_EXTENDED_COUNT, count));
            return stack;
        }
        return inner;
    }

    /**
     * @param context For deserializing the value.
     * @param valueType The type of the value.
     * @param tag Its tag in the text.
     * @return The value.
     * @throws RuntimeException If the tag is not a value of the type.
     */
    public static <T extends IValue> T fromText(ValueDeseralizationContext context, IValueType<T> valueType, Tag tag) {
        CompoundTag full;
        if (valueType == ValueTypes.LIST || (tag instanceof CompoundTag compound && compound.isEmpty() && valueType != ValueTypes.OBJECT_ITEMSTACK)) {
            if (!(tag instanceof CompoundTag compound)) {
                throw new IllegalArgumentException("Expected a compound: " + tag);
            }
            full = compound;
        } else if (valueType == ValueTypes.OBJECT_ITEMSTACK && tag instanceof CompoundTag stack) {
            CompoundTag item = new CompoundTag();
            CompoundTag stackWithoutCount = stack.copy();
            stackWithoutCount.remove(KEY_EXTENDED_COUNT);
            item.put(KEY_STACK, stackWithoutCount);
            stack.getInt(KEY_EXTENDED_COUNT).ifPresent(count -> item.putInt(KEY_EXTENDED_COUNT, count));
            full = new CompoundTag();
            full.put(KEY_VALUE, item);
        } else {
            full = new CompoundTag();
            full.put(KEY_VALUE, tag);
        }
        return ExpressionTrees.deserializeValue(context, valueType, full);
    }

}
