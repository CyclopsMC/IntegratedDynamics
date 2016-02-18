package org.cyclops.integrateddynamics.block;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.config.extendedconfig.BlockContainerConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.client.render.tileentity.RenderTileEntitySqueezer;
import org.cyclops.integrateddynamics.tileentity.TileSqueezer;

/**
 * Config for {@link BlockSqueezer}.
 * @author rubensworks
 */
public class BlockSqueezerConfig extends BlockContainerConfig {

    /**
     * The unique instance.
     */
    public static BlockSqueezerConfig _instance;

    /**
     * Make a new instance.
     */
    public BlockSqueezerConfig() {
        super(
            IntegratedDynamics._instance,
            true,
            "squeezer",
            null,
            BlockSqueezer.class
        );
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onRegistered() {
        super.onRegistered();
        getMod().getProxy().registerRenderer(TileSqueezer.class, new RenderTileEntitySqueezer());
    }
}
