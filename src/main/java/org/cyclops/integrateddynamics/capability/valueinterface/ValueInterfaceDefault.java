package org.cyclops.integrateddynamics.capability.valueinterface;

import org.cyclops.integrateddynamics.api.evaluate.IValueInterface;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;

import java.util.Collections;
import java.util.List;

/**
 * Default implementation of {@link IValueInterface}.
 * @author rubensworks
 */
public class ValueInterfaceDefault implements IValueInterface {

    private List<IValue> values;

    public ValueInterfaceDefault(List<IValue> values) {
        this.values = Collections.unmodifiableList(values);
    }

    @Override
    public List<IValue> getValues() {
        return values;
    }

    public void setValues(List<IValue> values) {
        this.values = Collections.unmodifiableList(values);
    }
}
