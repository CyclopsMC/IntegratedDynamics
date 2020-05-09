package org.cyclops.integrateddynamics.api.network;

import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.init.IRegistry;

import java.util.Collection;

/**
 * Registry for {@link INetworkCraftingHandler}.
 * @author rubensworks
 */
public interface INetworkCraftingHandlerRegistry extends IRegistry {

    /**
     * Register a new crafting handler.
     * @param craftingHandler The crafting handler.
     * @param <C> The crafting handler type.
     * @return The registered crafting handler.
     */
    public <C extends INetworkCraftingHandler> C register(C craftingHandler);

    /**
     * @return All registered crafting handlers.
     */
    public Collection<INetworkCraftingHandler> getCraftingHandlers();

    /**
     * Check if at least one crafting handler is crafting the given instance.
     * @param network The network to craft in.
     * @param ingredientsNetwork The ingredients network to craft in.
     * @param channel A channel.
     * @param ingredientComponent The ingredient component.
     * @param instance The instance to craft.
     * @param matchCondition The condition under which the instance should be crafted.
     * @param <T> The instance type.
     * @param <M> The matching condition parameter, may be Void.
     * @return If the instance is being crafted.
     */
    public default <T, M> boolean isCrafting(INetwork network, IPositionedAddonsNetworkIngredients<T, M> ingredientsNetwork, int channel,
                                             IngredientComponent<T, M> ingredientComponent, T instance, M matchCondition) {
        for (INetworkCraftingHandler craftingHandler : getCraftingHandlers()) {
            if (craftingHandler.isCrafting(network, ingredientsNetwork, channel, ingredientComponent, instance, matchCondition)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if at least one crafting handler is applicable for the given channel.
     * @param network The network to craft in.
     * @param ingredientsNetwork The ingredients network to craft in.
     * @param channel A channel.
     * @param <T> The instance type.
     * @param <M> The matching condition parameter, may be Void.
     * @return If we can craft in the given channel.
     */
    public default <T, M> boolean canCraft(INetwork network, IPositionedAddonsNetworkIngredients<T, M> ingredientsNetwork, int channel) {
        for (INetworkCraftingHandler craftingHandler : getCraftingHandlers()) {
            if (craftingHandler.canCraft(network, ingredientsNetwork, channel)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Start a crafting job for the given instance in the given channel.
     * @param network The network to craft in.
     * @param ingredientsNetwork The ingredients network to craft in.
     * @param channel A channel.
     * @param ingredientComponent The ingredient component.
     * @param instance The instance to craft.
     * @param matchCondition The condition under which the instance should be crafted.
     * @param ignoreExistingJobs If running jobs for the given instance should be ignored,
     *                           and a new job should be started anyways.
     * @param <T> The instance type.
     * @param <M> The matching condition parameter, may be Void.
     * @return If a crafting job could be started.
     */
    public default <T, M> boolean craft(INetwork network, IPositionedAddonsNetworkIngredients<T, M> ingredientsNetwork, int channel,
                                        IngredientComponent<T, M> ingredientComponent, T instance, M matchCondition, boolean ignoreExistingJobs) {
        for (INetworkCraftingHandler craftingHandler : getCraftingHandlers()) {
            if (craftingHandler.craft(network, ingredientsNetwork, channel, ingredientComponent, instance, matchCondition, ignoreExistingJobs)) {
                return true;
            }
        }
        return false;
    }

}
