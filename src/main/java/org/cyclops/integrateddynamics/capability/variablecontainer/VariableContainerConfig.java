package org.cyclops.integrateddynamics.capability.variablecontainer;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.integrateddynamics.api.block.IVariableContainer;

/**
 * Config for the variable container capability.
 * @author rubensworks
 *
 */
public class VariableContainerConfig extends CapabilityConfig<IVariableContainer> {

    public static Capability<IVariableContainer> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public VariableContainerConfig() {
        super(
                CommonCapabilities._instance,
                "variable_container",
                IVariableContainer.class
        );
    }

}
