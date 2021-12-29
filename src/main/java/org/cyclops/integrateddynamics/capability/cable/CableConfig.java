package org.cyclops.integrateddynamics.capability.cable;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.integrateddynamics.api.block.cable.ICable;

/**
 * Config for the cable capability.
 * @author rubensworks
 *
 */
public class CableConfig extends CapabilityConfig<ICable> {

    public static Capability<ICable> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public CableConfig() {
        super(
                CommonCapabilities._instance,
                "cable",
                ICable.class
        );
    }

}
