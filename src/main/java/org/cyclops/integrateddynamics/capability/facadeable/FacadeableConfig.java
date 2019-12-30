package org.cyclops.integrateddynamics.capability.facadeable;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityStorage;
import org.cyclops.integrateddynamics.api.block.IFacadeable;

/**
 * Config for the facadeable capability.
 * @author rubensworks
 *
 */
public class FacadeableConfig extends CapabilityConfig<IFacadeable> {

    @CapabilityInject(IFacadeable.class)
    public static Capability<IFacadeable> CAPABILITY = null;

    public FacadeableConfig() {
        super(
                CommonCapabilities._instance,
                "facadeable",
                IFacadeable.class,
                new DefaultCapabilityStorage<IFacadeable>(),
                FacadeableDefault::new
        );
    }

}
