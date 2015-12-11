package org.cyclops.integrateddynamics.core.part.aspect.property;

import com.google.common.collect.Maps;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * A property that can be used inside aspects.
 * @author rubensworks
 */
public class AspectProperties implements IAspectProperties {

    private final Map<IAspectPropertyTypeInstance, IValue> values = Maps.newHashMap();

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
        return (V) values.get(type);
    }

    @Override
    public <T extends IValueType<V>, V extends IValue> void setValue(IAspectPropertyTypeInstance<T, V> type, V value) {
        values.put(type, value);
    }

    @Override
    public NBTTagCompound toNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList map = new NBTTagList();
        for(Map.Entry<IAspectPropertyTypeInstance, IValue> entry : values.entrySet()) {
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
