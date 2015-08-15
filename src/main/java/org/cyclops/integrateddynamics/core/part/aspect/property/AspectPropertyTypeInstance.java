package org.cyclops.integrateddynamics.core.part.aspect.property;

import lombok.Data;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;

/**
 * An instance of a property type with a label.
 * @author rubensworks
 */
@Data
public class AspectPropertyTypeInstance<T extends IValueType<V>, V extends IValue> {

    private final T type;
    private final String unlocalizedName;

}
