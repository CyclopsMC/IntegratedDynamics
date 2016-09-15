package org.cyclops.integrateddynamics.capability.cable;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityStorage;
import org.cyclops.integrateddynamics.api.block.cable.ICableFakeable;

/**
 * Config for the fakeable cable capability.
 * @author rubensworks
 *
 */
public class CableFakeableConfig extends CapabilityConfig {

    /**
     * The unique instance.
     */
    public static CableFakeableConfig _instance;

    @CapabilityInject(ICableFakeable.class)
    public static Capability<ICableFakeable> CAPABILITY = null;

    /**
     * Make a new instance.
     */
    public CableFakeableConfig() {
        super(
                CommonCapabilities._instance,
                true,
                "cableFakeable",
                "Cables that can become fake",
                ICableFakeable.class,
                new DefaultCapabilityStorage<ICableFakeable>(),
                CableFakeableDefault.class
        );
    }

    @Override
    public boolean isDisableable() {
        return false;
    }

}
