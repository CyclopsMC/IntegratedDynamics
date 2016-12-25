package org.cyclops.integrateddynamics.block;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.config.extendedconfig.BlockContainerConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.client.render.tileentity.RenderTileEntityDryingBasin;
import org.cyclops.integrateddynamics.tileentity.TileDryingBasin;

/**
 * Config for {@link BlockDryingBasin}.
 * @author rubensworks
 */
public class BlockDryingBasinConfig extends BlockContainerConfig {

    /**
     * The unique instance.
     */
    public static BlockDryingBasinConfig _instance;

    /**
     * Make a new instance.
     */
    public BlockDryingBasinConfig() {
        super(
            IntegratedDynamics._instance,
            true,
            "drying_basin",
            null,
            BlockDryingBasin.class
        );
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onRegistered() {
        super.onRegistered();
        getMod().getProxy().registerRenderer(TileDryingBasin.class, new RenderTileEntityDryingBasin());
    }
}
