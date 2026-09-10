package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.flag.FeatureFlags;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfig;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenWrenchConfig;

/**
 * Config for {@link ContainerWrenchConfig}.
 * @author rubensworks
 */
public class ContainerWrenchConfigConfig extends GuiConfig<ContainerWrenchConfig> {

    public ContainerWrenchConfigConfig() {
        super(IntegratedDynamics._instance,
                "wrench_config",
                eConfig -> new ContainerTypeData<>(ContainerWrenchConfig::new, FeatureFlags.VANILLA_SET));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & MenuAccess<ContainerWrenchConfig>> MenuScreens.ScreenConstructor<ContainerWrenchConfig, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenWrenchConfig::new);
    }

}
