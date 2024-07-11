package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeCategory;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeLPElementBase;

import java.util.Collections;
import java.util.Set;

/**
 * Base implementation of a value type category.
 * @author rubensworks
 */
public abstract class ValueTypeCategoryBase<V extends IValue> extends ValueTypeBase<V> implements IValueTypeCategory<V> {

    protected final Set<IValueType<?>> elements;

    /**
     * Make a new instance.
     * @param typeName The category name.
     * @param color The color.
     * @param colorFormat The color format.
     * @param elements The elements inside this category.
     * @param valueClass The value type class.
     */
    public ValueTypeCategoryBase(String typeName, int color, ChatFormatting colorFormat, Set<IValueType<?>> elements, Class<V> valueClass) {
        super(typeName, color, colorFormat, valueClass);
        this.elements = Collections.unmodifiableSet(elements);
    }

    /**
     * Make a new instance.
     * @param typeName The category name.
     * @param color The color.
     * @param colorFormat The color format.
     * @param valueClass The value type class.
     */
    public ValueTypeCategoryBase(String typeName, int color, ChatFormatting colorFormat, Class<V> valueClass) {
        super(typeName, color, colorFormat, valueClass);
        this.elements = null;
    }

    @Override
    public boolean isCategory() {
        return true;
    }

    @Override
    public V getDefault() {
        // Avoid crashes when default values would be used somewhere.
        return (V) ValueTypeBoolean.ValueBoolean.of(false);
    }

    @Override
    public String getTranslationKey() {
        return getUnlocalizedPrefix();
    }

    @Override
    public MutableComponent toCompactString(V value) {
        return null;
    }

    @Override
    public boolean correspondsTo(IValueType<?> valueType) {
        return valueType == this || elements == null ? true : elements.contains(valueType);
    }

    @Override
    public Tag serialize(ValueDeseralizationContext valueDeseralizationContext, V value) {
        throw new UnsupportedOperationException("This operation is not allowed");
    }

    @Override
    public V deserialize(ValueDeseralizationContext valueDeseralizationContext, Tag value) {
        throw new UnsupportedOperationException("This operation is not allowed");
    }

    protected String getModId() {
        return Reference.MOD_ID;
    }

    @Override
    public Set<IValueType<?>> getElements() {
        return Collections.unmodifiableSet(elements);
    }

    @Override
    public ValueTypeLPElementBase createLogicProgrammerElement() {
        return null;
    }
}
