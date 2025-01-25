package org.cyclops.integrateddynamics.core.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.integrateddynamics.core.client.gui.container.ContainerScreenPartOffset;

/**
 * @author rubensworks
 */
public class ContainerPartOffsetConfigScreenFactoryProvider extends GuiConfigScreenFactoryProvider<ContainerPartOffset> {
    @Override
    public <U extends Screen & MenuAccess<ContainerPartOffset>> MenuScreens.ScreenConstructor<ContainerPartOffset, U> getScreenFactory() {
        return new ScreenFactorySafe<>(new MenuScreens.ScreenConstructor<ContainerPartOffset, ContainerScreenPartOffset<ContainerPartOffset>>() {
            @Override
            public ContainerScreenPartOffset<ContainerPartOffset> create(ContainerPartOffset container, Inventory playerInventory, Component title) {
                return new ContainerScreenPartOffset<ContainerPartOffset>(container, playerInventory, title);
            }
        });
    }
}
