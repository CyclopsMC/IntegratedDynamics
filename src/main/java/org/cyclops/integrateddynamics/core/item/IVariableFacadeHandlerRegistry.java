package org.cyclops.integrateddynamics.core.item;

import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.cyclopscore.init.IRegistry;

/**
 * Registry for retrieving variable facade handlers.
 * @author rubensworks
 */
public interface IVariableFacadeHandlerRegistry extends IRegistry {

    /**
     * Register a new handler.
     * @param variableFacadeHandler The handler.
     */
    public void registerHandler(IVariableFacadeHandler variableFacadeHandler);

    /**
     * Checks the type of the given tag and uses the corresponding handler to retrieve its variable facade.
     * @param tagCompound The tag containing information that can be read and used to form a variable facade.
     * @return The variable facade handled by the appropriate handler.
     */
    public IVariableFacade handle(NBTTagCompound tagCompound);

    /**
     * Set the type of the given tag and uses the corresponding handler to write the variable facade.
     * @param tagCompound The tag that is used to write variable facade information to.
     * @param variableFacade The facade to write.
     * @param <F> The type of variable facade.
     */
    public <F extends IVariableFacade> void write(NBTTagCompound tagCompound, F variableFacade, IVariableFacadeHandler<F> handler);

}
