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
public class DynamicRedstoneConfig extends CapabilityConfig {

    /**
     * The unique instance.
     */
    public static DynamicRedstoneConfig _instance;

    @CapabilityInject(IDynamicRedstone.class)
    public static Capability<IDynamicRedstone> CAPABILITY = null;

    /**
     * Make a new instance.
     */
    public DynamicRedstoneConfig() {
        super(
                CommonCapabilities._instance,
                true,
                "dynamic_redstone",
                "Allows redstone level modifications.",
                IDynamicRedstone.class,
                new DefaultCapabilityStorage<IDynamicRedstone>(),
                DynamicRedstoneDefault.class
        );
    }

    @Override
    public boolean isDisableable() {
        return false;
    }

}
