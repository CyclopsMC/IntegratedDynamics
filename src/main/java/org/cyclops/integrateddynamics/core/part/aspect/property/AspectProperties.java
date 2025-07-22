package org.cyclops.integrateddynamics.core.part.aspect.property;

import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * A property that can be used inside aspects.
 * @author rubensworks
 */
public class AspectProperties implements IAspectProperties {

    private final Map<IAspectPropertyTypeInstance, IValue> values = Maps.newLinkedHashMap();

    /**
     * Make a new instance.
     * @param propertyTypes The types these properties will have. These will be used to initialize the default values.
     */
    public AspectProperties(Collection<IAspectPropertyTypeInstance> propertyTypes) {
        for(IAspectPropertyTypeInstance propertyType : propertyTypes) {
            values.put(propertyType, propertyType.getType().getDefault());
        }
    }

    /**
     * Only called for NBT serialization
     */
    public AspectProperties() {

    }

    @Override
    @Deprecated
    public Collection<IAspectPropertyTypeInstance> getTypes() {
        return Collections.unmodifiableCollection(values.keySet());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IValueType<V>, V extends IValue> V getValue(IAspectPropertyTypeInstance<T, V> type) {
        IValue value = values.get(type);
        if (value == null) {
            value = type.getType().getDefault();
        }
        return (V) value;
    }

    @Override
    public <T extends IValueType<V>, V extends IValue> void setValue(IAspectPropertyTypeInstance<T, V> type, V value) {
        values.put(type, value);
    }

    @Override
    public <T extends IValueType<V>, V extends IValue> void removeValue(IAspectPropertyTypeInstance<T, V> type) {
        values.remove(type);
    }

    @Override
    public void serialize(ValueOutput valueOutput) {
        ValueOutput.ValueOutputList map = valueOutput.childrenList("map");
        for(Map.Entry<IAspectPropertyTypeInstance, IValue> entry : values.entrySet()) {
            ValueOutput nbtEntry = map.addChild();
            nbtEntry.putString("key", entry.getKey().getType().getUniqueName().toString());
            nbtEntry.putString("label", entry.getKey().getTranslationKey());
            ValueHelpers.serializeRaw(nbtEntry.child("value"), entry.getValue());
        }
    }

    @Override
    public void deserialize(ValueInput valueInput) {
        values.clear();
        for (ValueInput entry : valueInput.childrenList("map").orElseThrow()) {
            String valueTypeName = entry.getString("key").orElseThrow();
            IValueType type = ValueTypes.REGISTRY.getValueType(ResourceLocation.parse(valueTypeName));
            if(type == null) {
                IntegratedDynamics.clog(org.apache.logging.log4j.Level.ERROR, String.format("Could not find value type with name %s, skipping loading.", valueTypeName));
            } else {
                IValue value = ValueHelpers.deserializeRaw(entry.child("value").orElseThrow(), type);
                String label = entry.getString("label").orElseThrow();
                if(value == null) {
                    IntegratedDynamics.clog(org.apache.logging.log4j.Level.ERROR, String.format("The value type %s could not load its value, using default.", valueTypeName));
                    value = type.getDefault();
                }
                values.put(new AspectPropertyTypeInstance(type, label), value);
            }
        }
    }

    @SuppressWarnings({"CloneDoesntCallSuperClone", "deprecation"})
    @Override
    public IAspectProperties clone() {
        IAspectProperties clone = new AspectProperties(getTypes());
        for(IAspectPropertyTypeInstance type : getTypes()) {
            clone.setValue(type, getValue(type));
        }
        return clone;

    }
}
