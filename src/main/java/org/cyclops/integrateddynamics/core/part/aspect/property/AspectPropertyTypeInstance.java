package org.cyclops.integrateddynamics.core.part.aspect.property;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import lombok.Data;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectPropertyTypeInstance;

/**
 * An instance of a property type with a onLabelPacket.
 * @author rubensworks
 */
@Data
public class AspectPropertyTypeInstance<T extends IValueType<V>, V extends IValue> implements IAspectPropertyTypeInstance<T, V> {

    private final T type;
    private final String unlocalizedName;
    private final Predicate<V> validator;

    public AspectPropertyTypeInstance(T type, String unlocalizedName) {
        this(type, unlocalizedName, Predicates.<V>alwaysTrue());
    }

    public AspectPropertyTypeInstance(T type, String unlocalizedName, Predicate<V> validator) {
        this.type = type;
        this.unlocalizedName = unlocalizedName;
        this.validator = validator;
    }
}
