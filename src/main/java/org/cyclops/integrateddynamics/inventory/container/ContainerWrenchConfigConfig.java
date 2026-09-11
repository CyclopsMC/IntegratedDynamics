package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.world.flag.FeatureFlags;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link ContainerWrenchConfig}.
 * @author rubensworks
 */
public class ContainerWrenchConfigConfig extends GuiConfigCommon<ContainerWrenchConfig, IModBase> {

    public ContainerWrenchConfigConfig() {
        super(IntegratedDynamics._instance,
                "wrench_config",
                eConfig -> new ContainerTypeData<>(ContainerWrenchConfig::new, FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerWrenchConfig> getScreenFactoryProvider() {
        return new ContainerWrenchConfigConfigScreenFactoryProvider();
    }

}
