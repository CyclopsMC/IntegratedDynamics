package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenWrenchConfig;

/**
 * @author rubensworks
 */
public class ContainerWrenchConfigConfigScreenFactoryProvider extends GuiConfigScreenFactoryProvider<ContainerWrenchConfig> {
    @Override
    public <U extends Screen & MenuAccess<ContainerWrenchConfig>> MenuScreens.ScreenConstructor<ContainerWrenchConfig, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenWrenchConfig::new);
    }
}
