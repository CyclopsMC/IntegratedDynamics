package org.cyclops.integrateddynamics.api.evaluate.variable;

import net.minecraft.resources.ResourceLocation;
import org.cyclops.cyclopscore.init.IRegistry;
import org.cyclops.integrateddynamics.api.item.IValueTypeVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandler;

import java.util.Collection;

/**
 * Registry for {@link IValueType}
 * @author rubensworks
 */
public interface IValueTypeRegistry extends IRegistry, IVariableFacadeHandler<IValueTypeVariableFacade> {

    public IValueTypeRegistryClient getClient();

    /**
     * Register a new value type.
     * @param valueType The part type.
     * @param <V> The value type.
     * @param <T> The value type type.
     * @return The registered value type.
     */
    public <V extends IValue, T extends IValueType<V>> T register(T valueType);

    /**
     * Register a new value category.
     * This registration can be overwritten, so only the last registered category is remembered.
     * @param category The category.
     * @param <V> The value type.
     * @param <T> The value type type.
     * @return The registered category.
     */
    public <V extends IValue, T extends IValueTypeCategory<V>> T registerCategory(T category);

    /**
     * Get the value type by name.
     * @param name The unique name.
     * @return The value type or null if not found.
     */
    public IValueType getValueType(ResourceLocation name);

    /**
     * @return All registered value types.
     */
    public Collection<IValueType> getValueTypes();

}
