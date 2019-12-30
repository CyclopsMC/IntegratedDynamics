package org.cyclops.integrateddynamics.tileentity;

import com.google.common.collect.Sets;
import net.minecraft.tileentity.TileEntityType;
import org.cyclops.cyclopscore.config.extendedconfig.TileEntityConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the {@link TileDelay}.
 * @author rubensworks
 *
 */
public class TileDelayConfig extends TileEntityConfig<TileDelay> {

    public TileDelayConfig() {
        super(
                IntegratedDynamics._instance,
                "delay",
                (eConfig) -> new TileEntityType<>(TileDelay::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_DELAY), null)
        );
    }

}
