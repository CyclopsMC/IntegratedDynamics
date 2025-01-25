package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLabeller;

/**
 * @author rubensworks
 */
public class ContainerLabellerConfigScreenFactoryProvider extends GuiConfigScreenFactoryProvider<ContainerLabeller> {
    @Override
    public <U extends Screen & MenuAccess<ContainerLabeller>> MenuScreens.ScreenConstructor<ContainerLabeller, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenLabeller::new);
    }
}
