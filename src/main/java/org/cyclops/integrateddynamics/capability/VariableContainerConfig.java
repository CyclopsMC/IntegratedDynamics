package org.cyclops.integrateddynamics.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityStorage;
import org.cyclops.integrateddynamics.api.block.IVariableContainer;

/**
 * Config for the variable container capability.
 * @author rubensworks
 *
 */
public class VariableContainerConfig extends CapabilityConfig {

    /**
     * The unique instance.
     */
    public static VariableContainerConfig _instance;

    @CapabilityInject(IVariableContainer.class)
    public static Capability<IVariableContainer> CAPABILITY = null;

    /**
     * Make a new instance.
     */
    public VariableContainerConfig() {
        super(
                CommonCapabilities._instance,
                true,
                "variableContainer",
                "Allows storage of variables.",
                IVariableContainer.class,
                new DefaultCapabilityStorage<IVariableContainer>(),
                VariableContainerDefault.class
        );
    }

    @Override
    public boolean isDisableable() {
        return false;
    }

}
