package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenMechanicalDryingBasin;

/**
 * @author rubensworks
 */
public class ContainerMechanicalDryingBasinConfigScreenFactoryProvider extends GuiConfigScreenFactoryProvider<ContainerMechanicalDryingBasin> {
    @Override
    public <U extends Screen & MenuAccess<ContainerMechanicalDryingBasin>> MenuScreens.ScreenConstructor<ContainerMechanicalDryingBasin, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenMechanicalDryingBasin::new);
    }
}
