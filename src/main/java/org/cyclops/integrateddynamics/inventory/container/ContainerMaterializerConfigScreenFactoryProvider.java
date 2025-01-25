package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenMaterializer;

/**
 * @author rubensworks
 */
public class ContainerMaterializerConfigScreenFactoryProvider extends GuiConfigScreenFactoryProvider<ContainerMaterializer> {
    @Override
    public <U extends Screen & MenuAccess<ContainerMaterializer>> MenuScreens.ScreenConstructor<ContainerMaterializer, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenMaterializer::new);
    }
}
