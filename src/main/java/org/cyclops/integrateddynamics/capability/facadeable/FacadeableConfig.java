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
public class FacadeableConfig extends CapabilityConfig {

    /**
     * The unique instance.
     */
    public static FacadeableConfig _instance;

    @CapabilityInject(IFacadeable.class)
    public static Capability<IFacadeable> CAPABILITY = null;

    /**
     * Make a new instance.
     */
    public FacadeableConfig() {
        super(
                CommonCapabilities._instance,
                true,
                "facadeable",
                "Can hold a facade",
                IFacadeable.class,
                new DefaultCapabilityStorage<IFacadeable>(),
                FacadeableDefault.class
        );
    }

    @Override
    public boolean isDisableable() {
        return false;
    }

}
