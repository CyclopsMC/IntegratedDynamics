package org.cyclops.integrateddynamics.tileentity;

import com.google.common.collect.Sets;
import net.minecraft.tileentity.TileEntityType;
import org.cyclops.cyclopscore.config.extendedconfig.TileEntityConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the {@link TileMechanicalSqueezer}.
 * @author rubensworks
 *
 */
public class TileMechanicalSqueezerConfig extends TileEntityConfig<TileMechanicalSqueezer> {

    public TileMechanicalSqueezerConfig() {
        super(
                IntegratedDynamics._instance,
                "mechanical_squeezer",
                (eConfig) -> new TileEntityType<>(TileMechanicalSqueezer::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_MECHANICAL_SQUEEZER), null)
        );
    }

}
