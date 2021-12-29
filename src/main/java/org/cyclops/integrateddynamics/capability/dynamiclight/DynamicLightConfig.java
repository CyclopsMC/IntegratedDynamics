package org.cyclops.integrateddynamics.capability.dynamiclight;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.integrateddynamics.api.block.IDynamicLight;

/**
 * Config for the dynamic light capability.
 * @author rubensworks
 *
 */
public class DynamicLightConfig extends CapabilityConfig<IDynamicLight> {

    public static Capability<IDynamicLight> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public DynamicLightConfig() {
        super(
                CommonCapabilities._instance,
                "dynamic_light",
                IDynamicLight.class
        );
    }

}
