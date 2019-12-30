package org.cyclops.integrateddynamics.capability.variablecontainer;

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
public class VariableContainerConfig extends CapabilityConfig<IVariableContainer> {

    @CapabilityInject(IVariableContainer.class)
    public static Capability<IVariableContainer> CAPABILITY = null;

    public VariableContainerConfig() {
        super(
                CommonCapabilities._instance,
                "variable_container",
                IVariableContainer.class,
                new DefaultCapabilityStorage<IVariableContainer>(),
                VariableContainerDefault::new
        );
    }

}
