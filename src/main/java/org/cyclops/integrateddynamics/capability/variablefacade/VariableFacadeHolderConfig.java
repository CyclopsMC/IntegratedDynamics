package org.cyclops.integrateddynamics.capability.variablefacade;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHolder;

/**
 * Config for the variable facade holder capability.
 * @author rubensworks
 *
 */
public class VariableFacadeHolderConfig extends CapabilityConfig<IVariableFacadeHolder> {

    public static Capability<IVariableFacadeHolder> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public VariableFacadeHolderConfig() {
        super(
                CommonCapabilities._instance,
                "variable_facade_holder",
                IVariableFacadeHolder.class
        );
    }

}
