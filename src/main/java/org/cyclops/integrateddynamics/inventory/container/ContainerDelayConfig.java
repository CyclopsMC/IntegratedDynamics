package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenDelay;

/**
 * Config for {@link ContainerDelay}.
 * @author rubensworks
 */
public class ContainerDelayConfig extends GuiConfig<ContainerDelay> {

    public ContainerDelayConfig() {
        super(IntegratedDynamics._instance,
                "delay",
                eConfig -> new MenuType<>(ContainerDelay::new, FeatureFlags.VANILLA_SET));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & MenuAccess<ContainerDelay>> MenuScreens.ScreenConstructor<ContainerDelay, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenDelay::new);
    }

}
