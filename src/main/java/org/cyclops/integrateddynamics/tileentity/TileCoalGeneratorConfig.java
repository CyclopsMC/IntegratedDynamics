package org.cyclops.integrateddynamics.tileentity;

import com.google.common.collect.Sets;
import net.minecraft.tileentity.TileEntityType;
import org.cyclops.cyclopscore.config.extendedconfig.TileEntityConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the {@link TileCoalGenerator}.
 * @author rubensworks
 *
 */
public class TileCoalGeneratorConfig extends TileEntityConfig<TileCoalGenerator> {

    public TileCoalGeneratorConfig() {
        super(
                IntegratedDynamics._instance,
                "coal_generator",
                (eConfig) -> new TileEntityType<>(TileCoalGenerator::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_COAL_GENERATOR), null)
        );
    }

}
