package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenCoalGenerator;

/**
 * @author rubensworks
 */
public class ContainerCoalGeneratorConfigScreenFactoryProvider extends GuiConfigScreenFactoryProvider<ContainerCoalGenerator> {
    @Override
    public <U extends Screen & MenuAccess<ContainerCoalGenerator>> MenuScreens.ScreenConstructor<ContainerCoalGenerator, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenCoalGenerator::new);
    }
}
