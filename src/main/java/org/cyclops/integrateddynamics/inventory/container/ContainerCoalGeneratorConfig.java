package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link ContainerCoalGenerator}.
 * @author rubensworks
 */
public class ContainerCoalGeneratorConfig extends GuiConfigCommon<ContainerCoalGenerator, IModBase> {

    public ContainerCoalGeneratorConfig() {
        super(IntegratedDynamics._instance,
                "coal_generator",
                eConfig -> new MenuType<>(ContainerCoalGenerator::new, FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerCoalGenerator> getScreenFactoryProvider() {
        return new ContainerCoalGeneratorConfigScreenFactoryProvider();
    }
}
