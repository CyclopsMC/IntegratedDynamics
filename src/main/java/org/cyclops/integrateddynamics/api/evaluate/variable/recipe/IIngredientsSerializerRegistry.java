package org.cyclops.integrateddynamics.api.evaluate.variable.recipe;

import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.cyclopscore.init.IRegistry;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.core.evaluate.variable.recipe.IIngredients;

/**
 * Registry for handling the serialization and deserialization of {@link IIngredients}.
 * @author rubensworks
 */
public interface IIngredientsSerializerRegistry extends IRegistry {

    /**
     * Register an ingredients serializer.
     * @param serializer The ingredients serializer.
     */
    public void registerSerializer(IIngredientsSerializer<? extends IIngredients> serializer);

    /**
     * Serialize the given ingredients.
     * @param value The ingredients to serialize.
     * @return The serialized ingredients value.
     */
    public NBTTagCompound serialize(IIngredients value);

    /**
     * Deserialize the given ingredients value.
     * @param value The ingredients value to deserialize.
     * @return The deserialized ingredients.
     * @throws EvaluationException If an error occurs while deserializing.
     */
    public IIngredients deserialize(NBTTagCompound value) throws EvaluationException;

}
