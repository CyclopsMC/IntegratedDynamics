package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.inventory.container.ContainerType;
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
                eConfig -> new ContainerType<>(ContainerDelay::new));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & IHasContainer<ContainerDelay>> ScreenManager.IScreenFactory<ContainerDelay, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenDelay::new);
    }

}
