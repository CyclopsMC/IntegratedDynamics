package org.cyclops.integrateddynamics.api.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.cyclopscore.init.IRegistry;

import javax.annotation.Nullable;

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
     * @param itemStack The item containing information that can be read and used to form a variable facade.
     * @return The variable facade handled by the appropriate handler.
     */
    public IVariableFacade handle(ItemStack itemStack);

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
     * @param handler The handler for writing the facade.
     * @param <F> The type of variable facade.
     */
    public <F extends IVariableFacade> void write(NBTTagCompound tagCompound, F variableFacade, IVariableFacadeHandler<F> handler);

    /**
     * Write the given variable facade to the given itemstack.
     * @param itemStack The itemstack to write to.
     * @param variableFacade The variable facade.
     * @param variableFacadeHandler The variable facade handler.
     * @return A copy of the given itemstack with the written variable facade.
     * @param <F> The variable facade type.
     */
    public <F extends IVariableFacade> ItemStack writeVariableFacadeItem(ItemStack itemStack, F variableFacade, IVariableFacadeHandler<F> variableFacadeHandler);

    /**
     * Write a new variable facade to the given itemstack.
     * @param generateId If a new id should be generated. Otherwise the previous facade id will be used or -1 if non existing.
     * @param itemStack The itemstack to write to.
     * @param variableFacadeHandler The variable facade handler.
     * @param variableFacadeFactory A factory for creating a variable facade.
     * @param player The player crafting the item.
     * @param block The block in which the facade was created.
     * @return A copy of the given itemstack with the written variable facade.
     * @param <F> The variable facade type.
     */
    public <F extends IVariableFacade> ItemStack writeVariableFacadeItem(boolean generateId, ItemStack itemStack, IVariableFacadeHandler<F> variableFacadeHandler, IVariableFacadeFactory<F> variableFacadeFactory, @Nullable EntityPlayer player, @Nullable Block block);

    /**
     * Create a new variable facade..
     * @param generateId If a new id should be generated. Otherwise the previous facade id will be used or -1 if non existing.
     * @param itemStack The itemstack to write to.
     * @param variableFacadeHandler The variable facade handler.
     * @param variableFacadeFactory A factory for creating a variable facade.
     * @return The resulting variable facade.
     * @param <F> The variable facade type.
     */
    public <F extends IVariableFacade> F writeVariableFacade(boolean generateId, ItemStack itemStack, IVariableFacadeHandler<F> variableFacadeHandler, IVariableFacadeFactory<F> variableFacadeFactory);

    /**
     * Copy the given itemstack and (possibly) assign a new id.
     * @param generateId If a new id should be generated. Otherwise -1 will be used.
     * @param itemStack The itemstack to copy, it is assumed to refer to a valid variable facade.
     * @return A copy of the given itemstack with the written variable facade.
     * @param <F> The variable facade type.
     */
    public <F extends IVariableFacade> ItemStack copy(boolean generateId, ItemStack itemStack);

    /**
     * Factory for creating variable facades.
     * @param <F> The variable facade type.
     */
    public static interface IVariableFacadeFactory<F extends IVariableFacade> {

        /**
         * Create a new variable facade.
         * @param generateId If a new id should be generated, otherwise id -1
         * @return The new variable facade.
         */
        public F create(boolean generateId);

        /**
         * Create a new variable facade.
         * @param id The id the facade should use.
         * @return The new variable facade.
         */
        public F create(int id);

    }

}
