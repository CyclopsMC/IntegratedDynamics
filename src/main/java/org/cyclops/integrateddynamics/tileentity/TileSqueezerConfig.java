package org.cyclops.integrateddynamics.tileentity;

import com.google.common.collect.Sets;
import net.minecraft.tileentity.TileEntityType;
import org.cyclops.cyclopscore.config.extendedconfig.TileEntityConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

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

}
