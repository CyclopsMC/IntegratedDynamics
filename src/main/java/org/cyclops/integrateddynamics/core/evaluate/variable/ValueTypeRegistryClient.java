package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.client.color.item.Constant;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.resources.Identifier;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeRegistryClient;

import java.util.*;

/**
 * @author rubensworks
 */
public class ValueTypeRegistryClient implements IValueTypeRegistryClient {

    private Map<IValueType, ItemModel.Unbaked> valueTypeModels = new IdentityHashMap<>();

    @Override
    public <V extends IValue, T extends IValueType<V>> void registerValueTypeModel(T valueType, Identifier modelLocation) {
        valueTypeModels.put(valueType, new BlockModelWrapper.Unbaked(modelLocation, List.of(new Constant(-1))));
    }

    @Override
    public <V extends IValue, T extends IValueType<V>> ItemModel.Unbaked getValueTypeModel(T valueType) {
        return valueTypeModels.get(valueType);
    }

    @Override
    public Collection<ItemModel.Unbaked> getValueTypeModels() {
        return Collections.unmodifiableCollection(valueTypeModels.values());
    }

}
