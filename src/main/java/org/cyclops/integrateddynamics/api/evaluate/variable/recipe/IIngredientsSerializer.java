package org.cyclops.integrateddynamics.api.evaluate.variable.recipe;

import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.core.evaluate.variable.recipe.IIngredients;

/**
 * A serialization action for ingredients.
 * @param <I> The ingredients type
 * @author rubensworks
 */
public interface IIngredientsSerializer<I extends IIngredients> {

    /**
     * @param ingredients The ingredients to test.
     * @return If this can serialize the given ingredients.
     */
    public boolean canHandle(IIngredients ingredients);

    /**
     * @return The unique name of this serializer.
     */
    public String getUniqueName();

    /**
     * Serialize the given ingredients.
     * @param ingredients The ingredients to serialize.
     * @return The serialized ingredients value.
     */
    public NBTTagCompound serialize(I ingredients);

    /**
     * Deserialize the given ingredients value.
     * @param value The ingredients value to deserialize.
     * @return The deserialized ingredients, null if deserialization failed.
     * @throws EvaluationException If something goes wrong while deserializing
     */
    public I deserialize(NBTTagCompound value) throws EvaluationException;

}
