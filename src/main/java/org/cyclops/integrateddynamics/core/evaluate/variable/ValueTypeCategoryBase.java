package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeCategory;

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
     */
    public ValueTypeCategoryBase(String typeName, int color, String colorFormat, Set<IValueType<?>> elements) {
        super(typeName, color, colorFormat);
        this.elements = Collections.unmodifiableSet(elements);
    }

    /**
     * Make a new instance.
     * @param typeName The category name.
     * @param color The color.
     * @param colorFormat The color format.
     */
    public ValueTypeCategoryBase(String typeName, int color, String colorFormat) {
        super(typeName, color, colorFormat);
        this.elements = null;
    }

    @Override
    public boolean isCategory() {
        return true;
    }

    @Override
    public V getDefault() {
        return null;
    }

    @Override
    public String getUnlocalizedName() {
        return getUnlocalizedPrefix() + ".name";
    }

    @Override
    public String toCompactString(V value) {
        return null;
    }

    @Override
    public boolean correspondsTo(IValueType valueType) {
        return valueType == this || elements == null ? true : elements.contains(valueType);
    }

    @Override
    public String serialize(V value) {
        throw new UnsupportedOperationException("This operation is not allowed");
    }

    @Override
    public V deserialize(String value) {
        throw new UnsupportedOperationException("This operation is not allowed");
    }

    protected String getModId() {
        return Reference.MOD_ID;
    }

}
