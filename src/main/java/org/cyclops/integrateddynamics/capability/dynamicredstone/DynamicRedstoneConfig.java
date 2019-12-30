package org.cyclops.integrateddynamics.capability.dynamicredstone;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityStorage;
import org.cyclops.integrateddynamics.api.block.IDynamicRedstone;

/**
 * Config for the dynamic redstone capability.
 * @author rubensworks
 *
 */
public class DynamicRedstoneConfig extends CapabilityConfig<IDynamicRedstone> {

    @CapabilityInject(IDynamicRedstone.class)
    public static Capability<IDynamicRedstone> CAPABILITY = null;

    public DynamicRedstoneConfig() {
        super(
                CommonCapabilities._instance,
                "dynamic_redstone",
                IDynamicRedstone.class,
                new DefaultCapabilityStorage<IDynamicRedstone>(),
                DynamicRedstoneDefault::new
        );
    }

}
