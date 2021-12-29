package org.cyclops.integrateddynamics.capability.partcontainer;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.integrateddynamics.api.part.IPartContainer;

/**
 * Config for the part container capability.
 * @author rubensworks
 *
 */
public class PartContainerConfig extends CapabilityConfig<IPartContainer> {

    public static Capability<IPartContainer> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public PartContainerConfig() {
        super(
                CommonCapabilities._instance,
                "part_container",
                IPartContainer.class
        );
    }

}
