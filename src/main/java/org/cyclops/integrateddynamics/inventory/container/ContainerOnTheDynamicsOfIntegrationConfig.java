package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.flag.FeatureFlags;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfig;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenOnTheDynamicsOfIntegration;

/**
 * Config for {@link ContainerOnTheDynamicsOfIntegration}.
 * @author rubensworks
 */
public class ContainerOnTheDynamicsOfIntegrationConfig extends GuiConfig<ContainerOnTheDynamicsOfIntegration> {

    public ContainerOnTheDynamicsOfIntegrationConfig() {
        super(IntegratedDynamics._instance,
                "on_the_dynamics_of_integration",
                eConfig -> new ContainerTypeData<>(ContainerOnTheDynamicsOfIntegration::new, FeatureFlags.VANILLA_SET));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & MenuAccess<ContainerOnTheDynamicsOfIntegration>> MenuScreens.ScreenConstructor<ContainerOnTheDynamicsOfIntegration, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenOnTheDynamicsOfIntegration::new);
    }

}
