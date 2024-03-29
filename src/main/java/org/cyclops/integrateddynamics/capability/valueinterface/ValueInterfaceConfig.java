package org.cyclops.integrateddynamics.capability.valueinterface;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.integrateddynamics.api.evaluate.IValueInterface;

/**
 * Config for the value interface capability.
 * @author rubensworks
 *
 */
public class ValueInterfaceConfig extends CapabilityConfig<IValueInterface> {

    public static Capability<IValueInterface> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public ValueInterfaceConfig() {
        super(
                CommonCapabilities._instance,
                "value_interface_provider",
                IValueInterface.class
        );
    }

}
