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
public class DynamicLightConfig extends CapabilityConfig<IDynamicLight> {

    @CapabilityInject(IDynamicLight.class)
    public static Capability<IDynamicLight> CAPABILITY = null;

    public DynamicLightConfig() {
        super(
                CommonCapabilities._instance,
                "dynamic_light",
                IDynamicLight.class,
                new DefaultCapabilityStorage<IDynamicLight>(),
                DynamicLightDefault::new
        );
    }

}
