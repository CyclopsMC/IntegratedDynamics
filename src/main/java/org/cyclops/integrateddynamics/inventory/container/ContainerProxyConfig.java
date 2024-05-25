package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
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
                eConfig -> new MenuType<>(ContainerProxy::new, FeatureFlags.VANILLA_SET));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & MenuAccess<ContainerProxy>> MenuScreens.ScreenConstructor<ContainerProxy, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenProxy::new);
    }

}
