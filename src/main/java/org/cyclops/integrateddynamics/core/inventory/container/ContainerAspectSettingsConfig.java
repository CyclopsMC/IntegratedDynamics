package org.cyclops.integrateddynamics.core.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.flag.FeatureFlags;
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
                eConfig -> new ContainerTypeData<>(ContainerAspectSettings::new, FeatureFlags.VANILLA_SET));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & MenuAccess<ContainerAspectSettings>> MenuScreens.ScreenConstructor<ContainerAspectSettings, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenAspectSettings::new);
    }

}
