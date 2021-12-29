package org.cyclops.integrateddynamics.api.evaluate.variable;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.init.IRegistry;
import org.cyclops.integrateddynamics.api.item.IValueTypeVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandler;

import java.util.Collection;

/**
 * Registry for {@link IValueType}
 * @author rubensworks
 */
public interface IValueTypeRegistry extends IRegistry, IVariableFacadeHandler<IValueTypeVariableFacade> {

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
     * Register a model resource location for the given value type.
     * @param <V> The value type.
     * @param <T> The value type type.
     * @param valueType The value type.
     * @param modelLocation The model resource location.
     */
    @OnlyIn(Dist.CLIENT)
    public <V extends IValue, T extends IValueType<V>> void registerValueTypeModel(T valueType, ResourceLocation modelLocation);

    /**
     * Get the model resource location of the given value type.
     * @param <V> The value type.
     * @param <T> The value type type.
     * @param valueType The value type.
     * @return The model resource location.
     */
    @OnlyIn(Dist.CLIENT)
    public <V extends IValue, T extends IValueType<V>> ResourceLocation getValueTypeModel(T valueType);

    /**
     * Get all registered model resource locations for the value types.
     * @return All model resource locations.
     */
    @OnlyIn(Dist.CLIENT)
    public Collection<ResourceLocation> getValueTypeModels();

    /**
     * @return All registered value types.
     */
    public Collection<IValueType> getValueTypes();

}
