package org.cyclops.integrateddynamics.capability.valueinterface;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityStorage;
import org.cyclops.integrateddynamics.api.evaluate.IValueInterface;

/**
 * Config for the value interface capability.
 * @author rubensworks
 *
 */
public class ValueInterfaceConfig extends CapabilityConfig<IValueInterface> {

    @CapabilityInject(IValueInterface.class)
    public static Capability<IValueInterface> CAPABILITY = null;

    public ValueInterfaceConfig() {
        super(
                CommonCapabilities._instance,
                "value_interface_provider",
                IValueInterface.class,
                new DefaultCapabilityStorage<IValueInterface>(),
                () -> new ValueInterfaceDefault(null)
        );
    }

}
