package org.cyclops.integrateddynamics.core.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.integrateddynamics.core.client.gui.container.ContainerScreenPartSettings;

/**
 * @author rubensworks
 */
public class ContainerPartSettingsConfigScreenFactoryProvider extends GuiConfigScreenFactoryProvider<ContainerPartSettings> {
    @Override
    public <U extends Screen & MenuAccess<ContainerPartSettings>> MenuScreens.ScreenConstructor<ContainerPartSettings, U> getScreenFactory() {
        return new ScreenFactorySafe<>(new MenuScreens.ScreenConstructor<ContainerPartSettings, ContainerScreenPartSettings<ContainerPartSettings>>() {
            @Override
            public ContainerScreenPartSettings<ContainerPartSettings> create(ContainerPartSettings container, Inventory playerInventory, Component title) {
                return new ContainerScreenPartSettings<ContainerPartSettings>(container, playerInventory, title);
            }
        });
    }
}
