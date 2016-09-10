package org.cyclops.integrateddynamics.capability.dynamiclight;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityStorage;
import org.cyclops.integrateddynamics.api.block.IDynamicLight;

/**
 * Config for the dynamic light capability.
 * @author rubensworks
 *
 */
public class DynamicLightConfig extends CapabilityConfig {

    /**
     * The unique instance.
     */
    public static DynamicLightConfig _instance;

    @CapabilityInject(IDynamicLight.class)
    public static Capability<IDynamicLight> CAPABILITY = null;

    /**
     * Make a new instance.
     */
    public DynamicLightConfig() {
        super(
                CommonCapabilities._instance,
                true,
                "dynamicLight",
                "Allows light level modifications.",
                IDynamicLight.class,
                new DefaultCapabilityStorage<IDynamicLight>(),
                DynamicLightDefault.class
        );
    }

    @Override
    public boolean isDisableable() {
        return false;
    }

}
