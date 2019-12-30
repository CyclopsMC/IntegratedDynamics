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
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenProxy;

/**
 * Config for {@link ContainerProxy}.
 * @author rubensworks
 */
public class ContainerProxyConfig extends GuiConfig<ContainerProxy> {

    public ContainerProxyConfig() {
        super(IntegratedDynamics._instance,
                "proxy",
                eConfig -> new ContainerType<>(ContainerProxy::new));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & IHasContainer<ContainerProxy>> ScreenManager.IScreenFactory<ContainerProxy, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenProxy::new);
    }

}
