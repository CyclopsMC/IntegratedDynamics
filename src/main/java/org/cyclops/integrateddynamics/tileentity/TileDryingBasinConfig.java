package org.cyclops.integrateddynamics.tileentity;

import com.google.common.collect.Sets;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.config.extendedconfig.TileEntityConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.client.render.tileentity.RenderTileEntityDryingBasin;

/**
 * Config for the {@link TileDryingBasin}.
 * @author rubensworks
 *
 */
public class TileDryingBasinConfig extends TileEntityConfig<TileDryingBasin> {

    public TileDryingBasinConfig() {
        super(
                IntegratedDynamics._instance,
                "drying_basin",
                (eConfig) -> new TileEntityType<>(TileDryingBasin::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_DRYING_BASIN), null)
        );
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onRegistered() {
        super.onRegistered();
        getMod().getProxy().registerRenderer(getInstance(), RenderTileEntityDryingBasin::new);
    }

}
