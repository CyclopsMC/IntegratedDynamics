package org.cyclops.integrateddynamics.capability.cable;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.integrateddynamics.api.block.cable.ICableFakeable;

/**
 * Config for the fakeable cable capability.
 * @author rubensworks
 *
 */
public class CableFakeableConfig extends CapabilityConfig<ICableFakeable> {

    public static Capability<ICableFakeable> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public CableFakeableConfig() {
        super(
                CommonCapabilities._instance,
                "cableFakeable",
                ICableFakeable.class
        );
    }

}
