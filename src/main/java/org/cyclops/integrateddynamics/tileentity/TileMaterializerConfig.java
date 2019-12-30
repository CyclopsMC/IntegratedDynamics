package org.cyclops.integrateddynamics.tileentity;

import com.google.common.collect.Sets;
import net.minecraft.tileentity.TileEntityType;
import org.cyclops.cyclopscore.config.extendedconfig.TileEntityConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the {@link TileMaterializer}.
 * @author rubensworks
 *
 */
public class TileMaterializerConfig extends TileEntityConfig<TileMaterializer> {

    public TileMaterializerConfig() {
        super(
                IntegratedDynamics._instance,
                "materializer",
                (eConfig) -> new TileEntityType<>(TileMaterializer::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_MATERIALIZER), null)
        );
    }

}
