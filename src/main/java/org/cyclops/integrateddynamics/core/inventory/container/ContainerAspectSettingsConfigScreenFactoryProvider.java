package org.cyclops.integrateddynamics.core.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.integrateddynamics.core.client.gui.container.ContainerScreenAspectSettings;

/**
 * @author rubensworks
 */
public class ContainerAspectSettingsConfigScreenFactoryProvider extends GuiConfigScreenFactoryProvider<ContainerAspectSettings> {
    @Override
    public <U extends Screen & MenuAccess<ContainerAspectSettings>> MenuScreens.ScreenConstructor<ContainerAspectSettings, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenAspectSettings::new);
    }
}
