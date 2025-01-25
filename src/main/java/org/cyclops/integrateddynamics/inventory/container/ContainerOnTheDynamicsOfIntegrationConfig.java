package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.world.flag.FeatureFlags;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link ContainerOnTheDynamicsOfIntegration}.
 * @author rubensworks
 */
public class ContainerOnTheDynamicsOfIntegrationConfig extends GuiConfigCommon<ContainerOnTheDynamicsOfIntegration, IModBase> {

    public ContainerOnTheDynamicsOfIntegrationConfig() {
        super(IntegratedDynamics._instance,
                "on_the_dynamics_of_integration",
                eConfig -> new ContainerTypeData<>(ContainerOnTheDynamicsOfIntegration::new, FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerOnTheDynamicsOfIntegration> getScreenFactoryProvider() {
        return new ContainerOnTheDynamicsOfIntegrationConfigScreenFactoryProvider();
    }
}
