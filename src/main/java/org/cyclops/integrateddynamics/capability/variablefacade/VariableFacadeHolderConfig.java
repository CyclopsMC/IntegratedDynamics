package org.cyclops.integrateddynamics.capability.variablefacade;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityStorage;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHolder;

/**
 * Config for the variable facade holder capability.
 * @author rubensworks
 *
 */
public class VariableFacadeHolderConfig extends CapabilityConfig<IVariableFacadeHolder> {


    @CapabilityInject(IVariableFacadeHolder.class)
    public static Capability<IVariableFacadeHolder> CAPABILITY = null;

    public VariableFacadeHolderConfig() {
        super(
                CommonCapabilities._instance,
                "variable_facade_holder",
                IVariableFacadeHolder.class,
                new DefaultCapabilityStorage<IVariableFacadeHolder>(),
                () -> new VariableFacadeHolderDefault(ItemStack.EMPTY)
        );
    }

}
