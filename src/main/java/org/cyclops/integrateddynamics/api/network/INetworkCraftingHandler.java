package org.cyclops.integrateddynamics.api.network;

import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;

/**
 * A handler that adds crafting capabilities to networks.
 * @author rubensworks
 */
public interface INetworkCraftingHandler {

    /**
     * Check if the given instance is being crafted.
     * @param network The network to craft in.
     * @param ingredientsNetwork The ingredients network to craft in.
     * @param channel A channel.
     * @param ingredientComponent The ingredient component.
     * @param instance The instance to craft.
     * @param matchCondition The condition under which the instance should be crafted.
     * @param <T> The instance type.
     * @param <M> The matching condition parameter, may be Void. Instances MUST properly implement the equals method.
     * @return If a crafting job exists for the given instance.
     */
    public <T, M> boolean isCrafting(INetwork network, IPositionedAddonsNetworkIngredients<T, M> ingredientsNetwork, int channel,
                                     IngredientComponent<T, M> ingredientComponent, T instance, M matchCondition);

    /**
     * Check if this handler is applicable for the given channel.
     * @param network The network to craft in.
     * @param ingredientsNetwork The ingredients network to craft in.
     * @param channel A channel.
     * @param <T> The instance type.
     * @param <M> The matching condition parameter, may be Void. Instances MUST properly implement the equals method.
     * @return If we can craft in the given channel.
     */
    public <T, M> boolean canCraft(INetwork network, IPositionedAddonsNetworkIngredients<T, M> ingredientsNetwork, int channel);

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
     *                           If true, and a job already existed, then this method MUST return true.
     * @param <T> The instance type.
     * @param <M> The matching condition parameter, may be Void. Instances MUST properly implement the equals method.
     * @return If a crafting job could be started.
     */
    public <T, M> boolean craft(INetwork network, IPositionedAddonsNetworkIngredients<T, M> ingredientsNetwork, int channel,
                                IngredientComponent<T, M> ingredientComponent, T instance, M matchCondition,
                                boolean ignoreExistingJobs);

}
