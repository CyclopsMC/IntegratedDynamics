package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
                eConfig -> new ContainerTypeData<>(ContainerOnTheDynamicsOfIntegration::new));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & IHasContainer<ContainerOnTheDynamicsOfIntegration>> ScreenManager.IScreenFactory<ContainerOnTheDynamicsOfIntegration, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenOnTheDynamicsOfIntegration::new);
    }

}
