package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammer;

/**
 * @author rubensworks
 */
public class ContainerLogicProgrammerConfigScreenFactoryProvider extends GuiConfigScreenFactoryProvider<ContainerLogicProgrammer> {
    @Override
    public <U extends Screen & MenuAccess<ContainerLogicProgrammer>> MenuScreens.ScreenConstructor<ContainerLogicProgrammer, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenLogicProgrammer::new);
    }
}
