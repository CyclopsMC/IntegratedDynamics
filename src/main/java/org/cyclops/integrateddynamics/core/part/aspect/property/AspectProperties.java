package org.cyclops.integrateddynamics.core.part.aspect.property;

import com.google.common.collect.Maps;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.persist.nbt.INBTSerializable;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * A property that can be used inside aspects.
 * @author rubensworks
 */
public class AspectProperties implements INBTSerializable {

    private final Map<AspectPropertyTypeInstance, IValue> values = Maps.newHashMap();

    /**
     * Make a new instance.
     * @param propertyTypes The types these properties will have. These will be used to initialize the default values.
     */
    public AspectProperties(Collection<AspectPropertyTypeInstance> propertyTypes) {
        for(AspectPropertyTypeInstance propertyType : propertyTypes) {
            values.put(propertyType, propertyType.getType().getDefault());
        }
    }

    /**
     * @return The types.
     */
    public Collection<AspectPropertyTypeInstance> getTypes() {
        return Collections.unmodifiableCollection(values.keySet());
    }

    /**
     * Get the value of the given type.
     * @param type The type to get the value from.
     * @param <T> The value type type.
     * @param <V> The value type.
     * @return The value.
     */
    @SuppressWarnings("unchecked")
    public <T extends IValueType<V>, V extends IValue> V getValue(AspectPropertyTypeInstance<T, V> type) {
        return (V) values.get(type);
    }

    /**
     * Set the value for the given type.
     * @param type The type to get the value from.
     * @param <T> The value type type.
     * @param <V> The value type.
     * @param value The value.
     */
    public <T extends IValueType<V>, V extends IValue> void setValue(AspectPropertyTypeInstance<T, V> type, V value) {
        values.put(type, value);
    }

    @Override
    public NBTTagCompound toNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList map = new NBTTagList();
        for(Map.Entry<AspectPropertyTypeInstance, IValue> entry : values.entrySet()) {
            NBTTagCompound nbtEntry = new NBTTagCompound();
            nbtEntry.setString("key", entry.getKey().getType().getUnlocalizedName());
            nbtEntry.setString("label", entry.getKey().getUnlocalizedName());
            nbtEntry.setString("value", entry.getKey().getType().serialize(entry.getValue()));
            map.appendTag(nbtEntry);
        }
        tag.setTag("map", map);
        return tag;
    }

    @Override
    public void fromNBT(NBTTagCompound tag) {
        values.clear();
        NBTTagList map = tag.getTagList("map", MinecraftHelpers.NBTTag_Types.NBTTagCompound.ordinal());
        for(int i = 0; i < map.tagCount(); i++) {
            NBTTagCompound nbtEntry = map.getCompoundTagAt(i);
            String valueTypeName = nbtEntry.getString("key");
            IValueType type = ValueTypes.REGISTRY.getValueType(valueTypeName);
            if(type == null) {
                IntegratedDynamics.clog(Level.ERROR, String.format("Could not find value type with name %s, skipping loading.", valueTypeName));
            } else {
                IValue value = type.deserialize(nbtEntry.getString("value"));
                String label = nbtEntry.getString("label");
                if(value == null) {
                    IntegratedDynamics.clog(Level.ERROR, String.format("The value type %s could not load its value, using default.", valueTypeName));
                    value = type.getDefault();
                }
                values.put(new AspectPropertyTypeInstance(type, label), value);
            }
        }
    }

    /**
     * @return A deep copy of the properties.
     */
    @SuppressWarnings("CloneDoesntCallSuperClone")
    public AspectProperties clone() {
        AspectProperties clone = new AspectProperties(getTypes());
        for(AspectPropertyTypeInstance type : getTypes()) {
            clone.setValue(type, getValue(type));
        }
        return clone;

    }
}
