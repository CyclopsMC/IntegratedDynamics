package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link ContainerDelay}.
 * @author rubensworks
 */
public class ContainerDelayConfig extends GuiConfigCommon<ContainerDelay, IModBase> {

    public ContainerDelayConfig() {
        super(IntegratedDynamics._instance,
                "delay",
                eConfig -> new MenuType<>(ContainerDelay::new, FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerDelay> getScreenFactoryProvider() {
        return new ContainerDelayConfigScreenFactoryProvider();
    }
}
