package org.cyclops.integrateddynamics.blockentity;

import org.cyclops.integrateddynamics.client.render.blockentity.RenderCable;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityMultipartTickingConfig;

/**
 * @author rubensworks
 */
public class BlockEntityMultipartTickingConfigClient {
    public void onRegistered(BlockEntityMultipartTickingConfig config) {
        config.getMod().getProxy().registerRenderer(config.getInstance(), RenderCable::new);
    }
}
