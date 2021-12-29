package org.cyclops.integrateddynamics.capability.facadeable;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.integrateddynamics.api.block.IFacadeable;

/**
 * Config for the facadeable capability.
 * @author rubensworks
 *
 */
public class FacadeableConfig extends CapabilityConfig<IFacadeable> {

    public static Capability<IFacadeable> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public FacadeableConfig() {
        super(
                CommonCapabilities._instance,
                "facadeable",
                IFacadeable.class
        );
    }

}
