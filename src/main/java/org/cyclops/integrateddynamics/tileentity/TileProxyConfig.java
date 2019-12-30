package org.cyclops.integrateddynamics.tileentity;

import com.google.common.collect.Sets;
import net.minecraft.tileentity.TileEntityType;
import org.cyclops.cyclopscore.config.extendedconfig.TileEntityConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the {@link TileProxy}.
 * @author rubensworks
 *
 */
public class TileProxyConfig extends TileEntityConfig<TileProxy> {

    public TileProxyConfig() {
        super(
                IntegratedDynamics._instance,
                "proxy",
                (eConfig) -> new TileEntityType<>(TileProxy::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_PROXY), null)
        );
    }

}
