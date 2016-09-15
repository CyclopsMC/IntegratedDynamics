package org.cyclops.integrateddynamics.capability.partcontainer;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityStorage;
import org.cyclops.integrateddynamics.api.part.IPartContainer;

/**
 * Config for the part container capability.
 * @author rubensworks
 *
 */
public class PartContainerConfig extends CapabilityConfig {

    /**
     * The unique instance.
     */
    public static PartContainerConfig _instance;

    @CapabilityInject(IPartContainer.class)
    public static Capability<IPartContainer> CAPABILITY = null;

    /**
     * Make a new instance.
     */
    public PartContainerConfig() {
        super(
                CommonCapabilities._instance,
                true,
                "partContainer",
                "A container that can hold parts.",
                IPartContainer.class,
                new DefaultCapabilityStorage<IPartContainer>(),
                PartContainerDefault.class
        );
    }

    @Override
    public boolean isDisableable() {
        return false;
    }

}
