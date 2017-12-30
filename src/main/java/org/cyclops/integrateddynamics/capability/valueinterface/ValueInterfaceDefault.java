package org.cyclops.integrateddynamics.capability.valueinterface;

import org.cyclops.integrateddynamics.api.evaluate.IValueInterface;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;

import java.util.Optional;

/**
 * Default implementation of {@link IValueInterface}.
 * @author rubensworks
 */
public class ValueInterfaceDefault implements IValueInterface {

    private IValue value;

    public ValueInterfaceDefault(IValue value) {
        this.value = value;
    }

    @Override
    public Optional<IValue> getValue() {
        return Optional.of(value);
    }

    public void setValue(IValue value) {
        this.value = value;
    }
}
