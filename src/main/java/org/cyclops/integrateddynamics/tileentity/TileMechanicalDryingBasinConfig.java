package org.cyclops.integrateddynamics.tileentity;

import com.google.common.collect.Sets;
import net.minecraft.tileentity.TileEntityType;
import org.cyclops.cyclopscore.config.extendedconfig.TileEntityConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the {@link TileMechanicalDryingBasin}.
 * @author rubensworks
 *
 */
public class TileMechanicalDryingBasinConfig extends TileEntityConfig<TileMechanicalDryingBasin> {

    public TileMechanicalDryingBasinConfig() {
        super(
                IntegratedDynamics._instance,
                "mechanical_drying_basin",
                (eConfig) -> new TileEntityType<>(TileMechanicalDryingBasin::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_MECHANICAL_DRYING_BASIN), null)
        );
    }

}
