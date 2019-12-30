package org.cyclops.integrateddynamics.tileentity;

import com.google.common.collect.Sets;
import net.minecraft.tileentity.TileEntityType;
import org.cyclops.cyclopscore.config.extendedconfig.TileEntityConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the {@link TileVariablestore}.
 * @author rubensworks
 *
 */
public class TileVariableStoreConfig extends TileEntityConfig<TileVariablestore> {

    public TileVariableStoreConfig() {
        super(
                IntegratedDynamics._instance,
                "variable_store",
                (eConfig) -> new TileEntityType<>(TileVariablestore::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_VARIABLE_STORE), null)
        );
    }

}
