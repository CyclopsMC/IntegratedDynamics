package org.cyclops.integrateddynamics.api.ingredient.capability;

import net.minecraftforge.common.util.LazyOptional;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetworkIngredients;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Default implementation of {@link IPositionedAddonsNetworkIngredientsHandler}.
 * @author rubensworks
 */
public class DefaultPositionedAddonsNetworkIngredientsHandler<T, M> implements IPositionedAddonsNetworkIngredientsHandler<T, M> {

    private final Function<INetwork, IPositionedAddonsNetworkIngredients<T, M>> networkRetriever;

    public DefaultPositionedAddonsNetworkIngredientsHandler(Function<INetwork, IPositionedAddonsNetworkIngredients<T, M>> networkRetriever) {
        this.networkRetriever = networkRetriever;
    }

    @Nullable
    @Override
    public LazyOptional<IPositionedAddonsNetworkIngredients<T, M>> getStorage(INetwork network) {
        return LazyOptional.of(() -> networkRetriever.apply(network));
    }
}
