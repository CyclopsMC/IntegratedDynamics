package org.cyclops.integrateddynamics.capability.variablefacade;

import net.minecraft.world.item.ItemStack;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHolder;

/**
 * {@link IVariableFacadeHolder} that stores facade info in the root of the item's NBT tag.
 * @author rubensworks
 */
public class VariableFacadeHolderDefault implements IVariableFacadeHolder {

    private final ItemStack itemStack;

    public VariableFacadeHolderDefault(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public IVariableFacade getVariableFacade() {
        return IntegratedDynamics._instance.getRegistryManager().
                getRegistry(IVariableFacadeHandlerRegistry.class).handle(itemStack);
    }
}
