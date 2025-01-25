package org.cyclops.integrateddynamics.core.inventory.container;

import net.minecraft.world.flag.FeatureFlags;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link ContainerAspectSettings}.
 * @author rubensworks
 */
public class ContainerAspectSettingsConfig extends GuiConfigCommon<ContainerAspectSettings, IntegratedDynamics> {

    public ContainerAspectSettingsConfig() {
        super(IntegratedDynamics._instance,
                "aspect_settings",
                eConfig -> new ContainerTypeData<>(ContainerAspectSettings::new, FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerAspectSettings> getScreenFactoryProvider() {
        return new ContainerAspectSettingsConfigScreenFactoryProvider();
    }
}
