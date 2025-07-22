package org.cyclops.integrateddynamics.api.evaluate.variable;

import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;

/**
 * @author rubensworks
 */
public interface IValueTypeRegistryClient {

    /**
     * Register a facadeModel resource location for the given value type.
     * @param <V> The value type.
     * @param <T> The value type type.
     * @param valueType The value type.
     * @param modelLocation The facadeModel resource location.
     */
    public <V extends IValue, T extends IValueType<V>> void registerValueTypeModel(T valueType, ResourceLocation modelLocation);

    /**
     * Get the facadeModel resource location of the given value type.
     *
     * @param <V>       The value type.
     * @param <T>       The value type type.
     * @param valueType The value type.
     * @return The unbaked facadeModel resource.
     */
    public <V extends IValue, T extends IValueType<V>> ItemModel.Unbaked getValueTypeModel(T valueType);

    /**
     * Get all registered facadeModel resource locations for the value types.
     * @return All facadeModel resource locations.
     */
    public Collection<ItemModel.Unbaked> getValueTypeModels();

}
