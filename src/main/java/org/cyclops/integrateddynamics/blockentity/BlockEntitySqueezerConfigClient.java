package org.cyclops.integrateddynamics.blockentity;

import org.cyclops.integrateddynamics.client.render.blockentity.RenderBlockEntitySqueezer;

/**
 * @author rubensworks
 */
public class BlockEntitySqueezerConfigClient {

    public void onRegistered(BlockEntitySqueezerConfig config) {
        config.getMod().getProxy().registerRenderer(config.getInstance(), RenderBlockEntitySqueezer::new);
    }

}
