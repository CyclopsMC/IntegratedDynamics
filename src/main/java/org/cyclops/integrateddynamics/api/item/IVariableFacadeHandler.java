package org.cyclops.integrateddynamics.api.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;

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
     *
     * @param valueDeseralizationContext
     * @param id The id that was read and needs to be inserted into the variable facade.
     * @param tagCompound The tag containing information that can be read and used to form a variable facade.
     * @return The variable facade
     */
    public F getVariableFacade(ValueDeseralizationContext valueDeseralizationContext, int id, CompoundTag tagCompound);

    /**
     * Set the variable facade for the given tag.
     * @param tagCompound The tag that is used to write variable facade information to.
     * @param variableFacade The facade to write.
     */
    public void setVariableFacade(CompoundTag tagCompound, F variableFacade);

}
