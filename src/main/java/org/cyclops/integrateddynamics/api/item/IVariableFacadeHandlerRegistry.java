package org.cyclops.integrateddynamics.api.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.cyclopscore.init.IRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;

import javax.annotation.Nullable;
import java.util.Collection;

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
     *
     * @param valueDeseralizationContext
     * @param itemStack The item containing information that can be read and used to form a variable facade.
     * @return The variable facade handled by the appropriate handler.
     */
    public IVariableFacade handle(ValueDeseralizationContext valueDeseralizationContext, ItemStack itemStack);

    /**
     * Checks the type of the given tag and uses the corresponding handler to retrieve its variable facade.
     *
     * @param valueDeseralizationContext
     * @param tagCompound The tag containing information that can be read and used to form a variable facade.
     * @return The variable facade handled by the appropriate handler.
     */
    public IVariableFacade handle(ValueDeseralizationContext valueDeseralizationContext, CompoundTag tagCompound);

    /**
     * Find a handler by name.
     * @param type The handler name.
     * @return The handler.
     */
    @Nullable
    public IVariableFacadeHandler getHandler(ResourceLocation type);

    /**
     * @return All registered handler names.
     */
    public Collection<String> getHandlerNames();

    /**
     * Set the type of the given tag and uses the corresponding handler to write the variable facade.
     *
     * @param <F>                        The type of variable facade.
     * @param valueDeseralizationContext
     * @param tagCompound                The tag that is used to write variable facade information to.
     * @param variableFacade             The facade to write.
     * @param handler                    The handler for writing the facade.
     */
    public <F extends IVariableFacade> void write(ValueDeseralizationContext valueDeseralizationContext, CompoundTag tagCompound, F variableFacade, IVariableFacadeHandler<F> handler);

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
     *
     * @param <F> The variable facade type.
     * @param generateId If a new id should be generated. Otherwise the previous facade id will be used or -1 if non existing.
     * @param itemStack The itemstack to write to.
     * @param variableFacadeHandler The variable facade handler.
     * @param variableFacadeFactory A factory for creating a variable facade.
     * @param level
     * @param player The player crafting the item.
     * @param blockState The block state in which the facade was created.
     * @return A copy of the given itemstack with the written variable facade.
     */
    public <F extends IVariableFacade> ItemStack writeVariableFacadeItem(boolean generateId, ItemStack itemStack, IVariableFacadeHandler<F> variableFacadeHandler, IVariableFacadeFactory<F> variableFacadeFactory, Level level, @Nullable Player player, @Nullable BlockState blockState);

    /**
     * Create a new variable facade..
     *
     * @param <F> The variable facade type.
     * @param generateId If a new id should be generated. Otherwise the previous facade id will be used or -1 if non existing.
 * @param itemStack The itemstack to write to.
 * @param variableFacadeHandler The variable facade handler.
 * @param variableFacadeFactory A factory for creating a variable facade.
 * @param valueDeseralizationContext
     * @return The resulting variable facade.
     */
    public <F extends IVariableFacade> F writeVariableFacade(boolean generateId, ItemStack itemStack, IVariableFacadeHandler<F> variableFacadeHandler, IVariableFacadeFactory<F> variableFacadeFactory, ValueDeseralizationContext valueDeseralizationContext);

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
