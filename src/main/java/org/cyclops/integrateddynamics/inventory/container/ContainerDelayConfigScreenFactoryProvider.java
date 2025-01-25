package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenDelay;

/**
 * @author rubensworks
 */
public class ContainerDelayConfigScreenFactoryProvider extends GuiConfigScreenFactoryProvider<ContainerDelay> {
    @Override
    public <U extends Screen & MenuAccess<ContainerDelay>> MenuScreens.ScreenConstructor<ContainerDelay, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenDelay::new);
    }
}
