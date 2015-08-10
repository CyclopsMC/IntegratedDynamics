package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.init.IRegistry;
import org.cyclops.integrateddynamics.core.item.IVariableFacadeHandler;
import org.cyclops.integrateddynamics.core.item.ValueTypeVariableFacade;

import java.util.Collection;

/**
 * Registry for {@link org.cyclops.integrateddynamics.core.evaluate.variable.IValueType}
 * @author rubensworks
 */
public interface IValueTypeRegistry extends IRegistry, IVariableFacadeHandler<ValueTypeVariableFacade> {

    /**
     * Register a new value type.
     * @param valueType The part type.
     * @param <V> The value type.
     * @param <T> The value type type.
     * @return The registered value type.
     */
    public <V extends IValue, T extends IValueType<V>> T register(T valueType);

    /**
     * Get the value type by name.
     * @param name The unique name.
     * @return The value type or null if not found.
     */
    public IValueType getValueType(String name);

    /**
     * Register a model resource location for the given value type.
     * @param valueType The value type.
     * @param modelLocation The model resource location.
     * @param <V> The value type.
     * @param <T> The value type type.
     */
    @SideOnly(Side.CLIENT)
    public <V extends IValue, T extends IValueType<V>> void registerValueTypeModel(T valueType, ModelResourceLocation modelLocation);

    /**
     * Get the model resource location of the given value type.
     * @param valueType The value type.
     * @param <V> The value type.
     * @param <T> The value type type.
     * @return The model resource location.
     */
    @SideOnly(Side.CLIENT)
    public <V extends IValue, T extends IValueType<V>> ModelResourceLocation getValueTypeModel(T valueType);

    /**
     * Get all registered model resource locations for the value types.
     * @return All model resource locations.
     */
    @SideOnly(Side.CLIENT)
    public Collection<ModelResourceLocation> getValueTypeModels();

    /**
     * @return All registered value types.
     */
    public Collection<IValueType> getValueTypes();

}
