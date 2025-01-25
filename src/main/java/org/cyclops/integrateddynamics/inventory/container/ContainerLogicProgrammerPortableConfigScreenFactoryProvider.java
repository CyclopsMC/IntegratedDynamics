package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerPortable;

/**
 * @author rubensworks
 */
public class ContainerLogicProgrammerPortableConfigScreenFactoryProvider extends GuiConfigScreenFactoryProvider<ContainerLogicProgrammerPortable> {
    @Override
    public <U extends Screen & MenuAccess<ContainerLogicProgrammerPortable>> MenuScreens.ScreenConstructor<ContainerLogicProgrammerPortable, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenLogicProgrammerPortable::new);
    }
}
