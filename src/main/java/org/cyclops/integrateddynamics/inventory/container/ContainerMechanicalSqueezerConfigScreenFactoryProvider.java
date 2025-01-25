package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenMechanicalSqueezer;

/**
 * @author rubensworks
 */
public class ContainerMechanicalSqueezerConfigScreenFactoryProvider extends GuiConfigScreenFactoryProvider<ContainerMechanicalSqueezer> {
    @Override
    public <U extends Screen & MenuAccess<ContainerMechanicalSqueezer>> MenuScreens.ScreenConstructor<ContainerMechanicalSqueezer, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenMechanicalSqueezer::new);
    }
}
