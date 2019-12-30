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
public class CableFakeableConfig extends CapabilityConfig<ICableFakeable> {

    @CapabilityInject(ICableFakeable.class)
    public static Capability<ICableFakeable> CAPABILITY = null;

    public CableFakeableConfig() {
        super(
                CommonCapabilities._instance,
                "cableFakeable",
                ICableFakeable.class,
                new DefaultCapabilityStorage<ICableFakeable>(),
                () -> new CableFakeableDefault() {
                    @Override
                    protected void sendUpdate() {

                    }
                }
        );
    }

}
