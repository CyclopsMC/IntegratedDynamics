package org.cyclops.integrateddynamics.api.part.aspect.property;

import com.google.common.base.Predicate;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;

/**
 * An instance of a property type with a onLabelPacket.
 * @author rubensworks
 */
public interface IAspectPropertyTypeInstance<T extends IValueType<V>, V extends IValue> {

    /**
     * @return The value type of this property.
     */
    public T getType();

    /**
     * @return The unique name of this property, also used for localization.
     */
    String getUnlocalizedName();

    /**
     * @return The value validator.
     */
    Predicate<V> getValidator();
}
