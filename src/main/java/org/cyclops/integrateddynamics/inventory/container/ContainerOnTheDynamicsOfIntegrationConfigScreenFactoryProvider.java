package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenOnTheDynamicsOfIntegration;

/**
 * @author rubensworks
 */
public class ContainerOnTheDynamicsOfIntegrationConfigScreenFactoryProvider extends GuiConfigScreenFactoryProvider<ContainerOnTheDynamicsOfIntegration> {
    @Override
    public <U extends Screen & MenuAccess<ContainerOnTheDynamicsOfIntegration>> MenuScreens.ScreenConstructor<ContainerOnTheDynamicsOfIntegration, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenOnTheDynamicsOfIntegration::new);
    }
}
