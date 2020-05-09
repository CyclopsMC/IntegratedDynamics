package org.cyclops.integrateddynamics.core.network;

import com.google.common.collect.Lists;
import org.cyclops.integrateddynamics.api.network.INetworkCraftingHandler;
import org.cyclops.integrateddynamics.api.network.INetworkCraftingHandlerRegistry;

import java.util.Collection;
import java.util.List;

/**
 * Registry for {@link INetworkCraftingHandler}.
 * @author rubensworks
 */
public final class NetworkCraftingHandlerRegistry implements INetworkCraftingHandlerRegistry {

    private static NetworkCraftingHandlerRegistry INSTANCE = new NetworkCraftingHandlerRegistry();

    private List<INetworkCraftingHandler> handlers = Lists.newArrayList();

    private NetworkCraftingHandlerRegistry() {

    }

    /**
     * @return The unique instance.
     */
    public static NetworkCraftingHandlerRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public <C extends INetworkCraftingHandler> C register(C craftingHandler) {
        handlers.add(craftingHandler);
        return craftingHandler;
    }

    @Override
    public Collection<INetworkCraftingHandler> getCraftingHandlers() {
        return handlers;
    }
}
