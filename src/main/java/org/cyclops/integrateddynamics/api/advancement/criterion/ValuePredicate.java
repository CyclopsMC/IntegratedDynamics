package org.cyclops.integrateddynamics.api.advancement.criterion;

import com.google.gson.JsonElement;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;

import java.util.Optional;

/**
 * A predicate for values of a certain type.
 * @author rubensworks
 */
public class ValuePredicate<V extends IValue> {

    public static final ValuePredicate ANY = new ValuePredicate<>(Optional.empty(), Optional.empty(), Optional.empty());

    private final Optional<IValueType> valueType;
    private final Optional<IValue> value;
    private final Optional<JsonElement> valueJson;

    public ValuePredicate(Optional<IValueType> valueType, Optional<IValue> value, Optional<JsonElement> valueJson) {
        this.valueType = valueType;
        this.value = value;
        this.valueJson = valueJson;
    }

    public Optional<IValueType> getValueType() {
        return valueType;
    }

    public Optional<IValue> getValue() {
        return value;
    }

    public Optional<JsonElement> getValueJson() {
        return valueJson;
    }

    public final boolean test(IValue value) {
        return (this.value.isEmpty() || ValueHelpers.areValuesEqual(this.value.get(), value))
                && (this.valueType.isEmpty() || value.getType() == this.valueType.get()) && testTyped((V) value);
    }

    protected boolean testTyped(V value) {
        return true;
    }

}
