package org.cyclops.integrateddynamics.tileentity;

import com.google.common.collect.Sets;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.config.extendedconfig.TileEntityConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.client.render.tileentity.RenderTileEntitySqueezer;

/**
 * Config for the {@link TileSqueezer}.
 * @author rubensworks
 *
 */
public class TileSqueezerConfig extends TileEntityConfig<TileSqueezer> {

    public TileSqueezerConfig() {
        super(
                IntegratedDynamics._instance,
                "squeezer",
                (eConfig) -> new TileEntityType<>(TileSqueezer::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_SQUEEZER), null)
        );
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onRegistered() {
        super.onRegistered();
        getMod().getProxy().registerRenderer(getInstance(), RenderTileEntitySqueezer::new);
    }

}
