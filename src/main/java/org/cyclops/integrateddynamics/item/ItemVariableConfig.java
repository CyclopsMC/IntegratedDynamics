package org.cyclops.integrateddynamics.item;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.cyclops.cyclopscore.config.extendedconfig.ItemClientConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfigCommon;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.capability.variablefacade.VariableFacadeHolderDefault;
import org.jetbrains.annotations.Nullable;

/**
 * Config for a variable item.
 * @author rubensworks
 */
public class ItemVariableConfig extends ItemConfigCommon<IntegratedDynamics> {

    public ItemVariableConfig() {
        super(
                IntegratedDynamics._instance,
                "variable",
                (eConfig, properties) -> new ItemVariable(properties)
        );
    }

    @Override
    public @Nullable ItemClientConfig<IntegratedDynamics> constructItemClientConfig() {
        return new ItemVariableConfigClient(this);
    }

    @SubscribeEvent
    protected void registerCapability(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.VariableFacade.ITEM, (stack, context) -> new VariableFacadeHolderDefault(stack), getInstance());
    }
}
