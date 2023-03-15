package org.cyclops.integrateddynamics.core.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfig;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.client.gui.container.ContainerScreenPartOffset;

/**
 * Config for {@link ContainerPartOffset}.
 * @author rubensworks
 */
public class ContainerPartOffsetConfig extends GuiConfig<ContainerPartOffset> {

    public ContainerPartOffsetConfig() {
        super(IntegratedDynamics._instance,
                "part_offset",
                eConfig -> new ContainerTypeData<>(ContainerPartOffset::new));
    }

    @OnlyIn(Dist.CLIENT)
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
