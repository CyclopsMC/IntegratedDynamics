package org.cyclops.integrateddynamics.core.tileentity;

import com.google.common.collect.Sets;
import net.minecraft.tileentity.TileEntityType;
import org.cyclops.cyclopscore.config.extendedconfig.TileEntityConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.tileentity.TileCoalGenerator;

/**
 * Config for the {@link TileMultipartTicking}.
 * @author rubensworks
 *
 */
public class TileMultipartTickingConfig extends TileEntityConfig<TileMultipartTicking> {

    public TileMultipartTickingConfig() {
        super(
                IntegratedDynamics._instance,
                "multipart_ticking",
                (eConfig) -> new TileEntityType<>(TileMultipartTicking::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_CABLE), null)
        );
    }

}
