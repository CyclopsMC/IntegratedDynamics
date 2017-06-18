package org.cyclops.integrateddynamics.capability.variablefacade;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityStorage;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHolder;
import org.cyclops.integrateddynamics.capability.variablecontainer.VariableContainerDefault;

/**
 * Config for the variable facade holder capability.
 * @author rubensworks
 *
 */
public class VariableFacadeHolderConfig extends CapabilityConfig {

    /**
     * The unique instance.
     */
    public static VariableFacadeHolderConfig _instance;

    @CapabilityInject(IVariableFacadeHolder.class)
    public static Capability<IVariableFacadeHolder> CAPABILITY = null;

    /**
     * Make a new instance.
     */
    public VariableFacadeHolderConfig() {
        super(
                CommonCapabilities._instance,
                true,
                "variable_facade_holder",
                "Allows holding of variable facades.",
                IVariableFacadeHolder.class,
                new DefaultCapabilityStorage<IVariableFacadeHolder>(),
                VariableContainerDefault.class
        );
    }

    @Override
    public boolean isDisableable() {
        return false;
    }

}
