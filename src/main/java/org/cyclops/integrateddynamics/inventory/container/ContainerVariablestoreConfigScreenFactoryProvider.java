package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenVariablestore;

/**
 * @author rubensworks
 */
public class ContainerVariablestoreConfigScreenFactoryProvider extends GuiConfigScreenFactoryProvider<ContainerVariablestore> {
    @Override
    public <U extends Screen & MenuAccess<ContainerVariablestore>> MenuScreens.ScreenConstructor<ContainerVariablestore, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenVariablestore::new);
    }
}
