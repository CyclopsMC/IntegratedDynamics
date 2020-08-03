package org.cyclops.integrateddynamics.core.inventory.container;

import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
                eConfig -> new ContainerTypeData<>(ContainerPartSettings::new));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & IHasContainer<ContainerPartSettings>> ScreenManager.IScreenFactory<ContainerPartSettings, U> getScreenFactory() {
        return new ScreenFactorySafe<>(new ScreenManager.IScreenFactory<ContainerPartSettings, ContainerScreenPartSettings<ContainerPartSettings>>() {
            @Override
            public ContainerScreenPartSettings<ContainerPartSettings> create(ContainerPartSettings container, PlayerInventory playerInventory, ITextComponent title) {
                return new ContainerScreenPartSettings<ContainerPartSettings>(container, playerInventory, title);
            }
        });
    }

}
