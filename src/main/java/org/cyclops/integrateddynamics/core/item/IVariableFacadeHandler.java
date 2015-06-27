package org.cyclops.integrateddynamics.core.item;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Handler for retrieving variable facades from items.
 * @param <F> The type of variable facade.
 * @author rubensworks
 */
public interface IVariableFacadeHandler<F extends IVariableFacade> {

    /**
     * @return The unique name of this type used to identity variables to this handler.
     */
    public String getTypeId();

    /**
     * Get the variable facade for the given tag.
     * @param id The id that was read and needs to be inserted into the variable facade.
     * @param tagCompound The tag containing information that can be read and used to form a variable facade.
     * @return The variable facade
     */
    public F getVariableFacade(int id, NBTTagCompound tagCompound);

    /**
     * Set the variable facade for the given tag.
     * @param tagCompound The tag that is used to write variable facade information to.
     * @param variableFacade The facade to write.
     */
    public void setVariableFacade(NBTTagCompound tagCompound, F variableFacade);

}
