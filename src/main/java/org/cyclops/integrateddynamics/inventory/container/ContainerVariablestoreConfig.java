package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link ContainerVariablestore}.
 * @author rubensworks
 */
public class ContainerVariablestoreConfig extends GuiConfigCommon<ContainerVariablestore, IModBase> {

    public ContainerVariablestoreConfig() {
        super(IntegratedDynamics._instance,
                "variablestore",
                eConfig -> new MenuType<>(ContainerVariablestore::new, FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerVariablestore> getScreenFactoryProvider() {
        return new ContainerVariablestoreConfigScreenFactoryProvider();
    }
}
