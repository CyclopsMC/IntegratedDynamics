package org.cyclops.integrateddynamics.core.inventory.container;

import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfig;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.client.gui.container.ContainerScreenAspectSettings;

/**
 * Config for {@link ContainerAspectSettings}.
 * @author rubensworks
 */
public class ContainerAspectSettingsConfig extends GuiConfig<ContainerAspectSettings> {

    public ContainerAspectSettingsConfig() {
        super(IntegratedDynamics._instance,
                "aspect_settings",
                eConfig -> new ContainerTypeData<>(ContainerAspectSettings::new));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & IHasContainer<ContainerAspectSettings>> ScreenManager.IScreenFactory<ContainerAspectSettings, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenAspectSettings::new);
    }

}
