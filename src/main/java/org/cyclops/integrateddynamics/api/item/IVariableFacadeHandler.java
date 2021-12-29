package org.cyclops.integrateddynamics.api.item;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.api.advancement.criterion.ValuePredicate;
import org.cyclops.integrateddynamics.api.advancement.criterion.VariableFacadePredicate;
import org.cyclops.integrateddynamics.api.advancement.criterion.VariablePredicate;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;

import javax.annotation.Nullable;

/**
 * Handler for retrieving variable facades from items.
 * Must be registered in {@link IVariableFacadeHandlerRegistry}.
 * @param <F> The type of variable facade.
 * @author rubensworks
 */
public interface IVariableFacadeHandler<F extends IVariableFacade> {

    /**
     * @return The unique name of this type used to identity variables to this handler.
     */
    public ResourceLocation getUniqueName();

    /**
     * Get the variable facade for the given tag.
     * @param id The id that was read and needs to be inserted into the variable facade.
     * @param tagCompound The tag containing information that can be read and used to form a variable facade.
     * @return The variable facade
     */
    public F getVariableFacade(int id, CompoundTag tagCompound);

    /**
     * Set the variable facade for the given tag.
     * @param tagCompound The tag that is used to write variable facade information to.
     * @param variableFacade The facade to write.
     */
    public void setVariableFacade(CompoundTag tagCompound, F variableFacade);

    /**
     * Deserialize the given JSON element to a variable predicate.
     * @param element The JSON element.
     * @param valueType The optional value type.
     * @param valuePredicate The value predicate.
     * @return The variable predicate.
     */
    default public VariablePredicate deserializeVariablePredicate(JsonObject element, @Nullable IValueType valueType, ValuePredicate valuePredicate) {
        return new VariablePredicate<>(IVariable.class, valueType, valuePredicate);
    }

    /**
     * Deserialize the given JSON element to a variable facade predicate.
     * @param element The JSON element.
     * @return The variable facade predicate.
     */
    default public VariableFacadePredicate deserializeVariableFacadePredicate(JsonObject element) {
        return new VariableFacadePredicate<>(IVariableFacade.class);
    }

}
