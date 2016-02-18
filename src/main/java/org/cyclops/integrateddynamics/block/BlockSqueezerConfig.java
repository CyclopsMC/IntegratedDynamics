package org.cyclops.integrateddynamics.block;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.config.extendedconfig.BlockContainerConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

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
        //getMod().getProxy().registerRenderer(TileDryingBasin.class, new RenderTileEntitySqueezer()); // TODO
    }
}
