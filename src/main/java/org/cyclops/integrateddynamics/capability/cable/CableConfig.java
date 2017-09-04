package org.cyclops.integrateddynamics.capability.cable;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityStorage;
import org.cyclops.integrateddynamics.api.block.cable.ICable;

/**
 * Config for the cable capability.
 * @author rubensworks
 *
 */
public class CableConfig extends CapabilityConfig<ICable> {

    /**
     * The unique instance.
     */
    public static CableConfig _instance;

    @CapabilityInject(ICable.class)
    public static Capability<ICable> CAPABILITY = null;

    /**
     * Make a new instance.
     */
    public CableConfig() {
        super(
                CommonCapabilities._instance,
                true,
                "cable",
                "Cables form networks",
                ICable.class,
                new DefaultCapabilityStorage<ICable>(),
                CableDefault.class
        );
    }

    @Override
    public boolean isDisableable() {
        return false;
    }

}
