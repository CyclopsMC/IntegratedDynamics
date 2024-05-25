package org.cyclops.integrateddynamics.core.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfig;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.client.gui.container.ContainerScreenPartSettings;

/**
 * Config for {@link ContainerPartSettings}.
 * @author rubensworks
 */
public class ContainerPartSettingsConfig extends GuiConfig<ContainerPartSettings> {

    public ContainerPartSettingsConfig() {
        super(IntegratedDynamics._instance,
                "part_settings",
                eConfig -> new ContainerTypeData<>(ContainerPartSettings::new, FeatureFlags.VANILLA_SET));
    }

    @OnlyIn(Dist.CLIENT)
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
