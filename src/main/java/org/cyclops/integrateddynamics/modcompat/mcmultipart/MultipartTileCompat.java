package org.cyclops.integrateddynamics.modcompat.mcmultipart;

import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.ref.MCMPCapabilities;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityProvider;
import org.cyclops.cyclopscore.modcompat.capabilities.SimpleCapabilityConstructor;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;

import javax.annotation.Nullable;

/**
 * Compatibility for MCMP tiles.
 * @author rubensworks
 */
public class MultipartTileCompat extends SimpleCapabilityConstructor<IMultipartTile, TileMultipartTicking> {

    @Override
    public Capability<IMultipartTile> getCapability() {
        return MCMPCapabilities.MULTIPART_TILE;
    }

    @Nullable
    @Override
    public ICapabilityProvider createProvider(TileMultipartTicking host) {
        return new DefaultCapabilityProvider<>(getCapability(), new PartTileMultipartTicking(host));
    }
}
