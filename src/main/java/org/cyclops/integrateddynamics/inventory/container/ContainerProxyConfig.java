package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link ContainerProxy}.
 * @author rubensworks
 */
public class ContainerProxyConfig extends GuiConfigCommon<ContainerProxy, IModBase> {

    public ContainerProxyConfig() {
        super(IntegratedDynamics._instance,
                "proxy",
                eConfig -> new MenuType<>(ContainerProxy::new, FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerProxy> getScreenFactoryProvider() {
        return new ContainerProxyConfigScreenFactoryProvider();
    }
}
