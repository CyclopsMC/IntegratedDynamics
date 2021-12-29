package org.cyclops.integrateddynamics.capability.dynamicredstone;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.integrateddynamics.api.block.IDynamicRedstone;

/**
 * Config for the dynamic redstone capability.
 * @author rubensworks
 *
 */
public class DynamicRedstoneConfig extends CapabilityConfig<IDynamicRedstone> {

    public static Capability<IDynamicRedstone> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public DynamicRedstoneConfig() {
        super(
                CommonCapabilities._instance,
                "dynamic_redstone",
                IDynamicRedstone.class
        );
    }

}
