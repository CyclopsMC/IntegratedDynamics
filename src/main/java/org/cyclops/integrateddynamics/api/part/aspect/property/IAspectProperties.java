package org.cyclops.integrateddynamics.api.part.aspect.property;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;

import java.util.Collection;

/**
 * A property that can be used inside aspects.
 * @author rubensworks
 */
public interface IAspectProperties {
    /**
     * Use this with caution!
     * Better to use {@link IAspect#getPropertyTypes()} instead because this object might hold deprecated elements.
     * @deprecated Use {@link IAspect#getPropertyTypes()}.
     * @return The types.
     */
    @Deprecated
    public Collection<IAspectPropertyTypeInstance> getTypes();

    /**
     * Get the value of the given type.
     * @param type The type to get the value from.
     * @param <T> The value type type.
     * @param <V> The value type.
     * @return The value.
     */
    public <T extends IValueType<V>, V extends IValue> V getValue(IAspectPropertyTypeInstance<T, V> type);

    /**
     * Set the value for the given type.
     * @param type The type to get the value from.
     * @param <T> The value type type.
     * @param <V> The value type.
     * @param value The value.
     */
    public <T extends IValueType<V>, V extends IValue> void setValue(IAspectPropertyTypeInstance<T, V> type, V value);

    /**
     * Remove the value of the given type.
     * @param type The type to get the value from.
     * @param <T> The value type type.
     * @param <V> The value type.
     */
    public <T extends IValueType<V>, V extends IValue> void removeValue(IAspectPropertyTypeInstance<T, V> type);

    /**
     * @return A deep copy of the properties.
     */
    @SuppressWarnings({"CloneDoesntCallSuperClone", "deprecation"})
    public IAspectProperties clone();


    /**
     * Serialize the data.
     * @param valueOutput The value output.
     */
    public void serialize(ValueOutput valueOutput);

    /**
     * Read the data and place it in this object.
     * The given tag will never be null, so make sure that all fields have a correct default value in case
     * the received tag would be null anyways.
     *
     * @param valueInput The value input.
     */
    public void deserialize(ValueInput valueInput);
}
