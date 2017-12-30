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

    /**
     * The unique instance.
     */
    public static ValueInterfaceConfig _instance;

    @CapabilityInject(IValueInterface.class)
    public static Capability<IValueInterface> CAPABILITY = null;

    /**
     * Make a new instance.
     */
    public ValueInterfaceConfig() {
        super(
                CommonCapabilities._instance,
                true,
                "value_interface_provider",
                "Capability for elements used for path construction",
                IValueInterface.class,
                new DefaultCapabilityStorage<IValueInterface>(),
                ValueInterfaceDefault.class
        );
    }

    @Override
    public boolean isDisableable() {
        return false;
    }

}
