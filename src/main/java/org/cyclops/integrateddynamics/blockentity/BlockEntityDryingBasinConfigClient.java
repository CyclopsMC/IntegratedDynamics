package org.cyclops.integrateddynamics.blockentity;

import org.cyclops.integrateddynamics.client.render.blockentity.RenderBlockEntityDryingBasin;

/**
 * @author rubensworks
 */
public class BlockEntityDryingBasinConfigClient {

    public void onRegistered(BlockEntityDryingBasinConfig config) {
        config.getMod().getProxy().registerRenderer(config.getInstance(), RenderBlockEntityDryingBasin::new);
    }

}
