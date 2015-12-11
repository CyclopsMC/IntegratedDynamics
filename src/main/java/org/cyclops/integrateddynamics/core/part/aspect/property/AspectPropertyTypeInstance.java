package org.cyclops.integrateddynamics.core.part.aspect.property;

import lombok.Data;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectPropertyTypeInstance;

/**
 * An instance of a property type with a label.
 * @author rubensworks
 */
@Data
public class AspectPropertyTypeInstance<T extends IValueType<V>, V extends IValue> implements IAspectPropertyTypeInstance<T, V> {

    private final T type;
    private final String unlocalizedName;

}
