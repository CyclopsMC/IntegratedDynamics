package org.cyclops.integrateddynamics.core.inventory.container;

import net.minecraft.world.flag.FeatureFlags;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link ContainerPartSettings}.
 * @author rubensworks
 */
public class ContainerPartSettingsConfig extends GuiConfigCommon<ContainerPartSettings, IntegratedDynamics> {

    public ContainerPartSettingsConfig() {
        super(IntegratedDynamics._instance,
                "part_settings",
                eConfig -> new ContainerTypeData<>(ContainerPartSettings::new, FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerPartSettings> getScreenFactoryProvider() {
        return new ContainerPartSettingsConfigScreenFactoryProvider();
    }
}
