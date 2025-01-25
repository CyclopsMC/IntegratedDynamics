package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link ContainerLogicProgrammer}.
 * @author rubensworks
 */
public class ContainerLogicProgrammerConfig extends GuiConfigCommon<ContainerLogicProgrammer, IModBase> {

    public ContainerLogicProgrammerConfig() {
        super(IntegratedDynamics._instance,
                "logic_programmer",
                eConfig -> new MenuType<>(ContainerLogicProgrammer::new, FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerLogicProgrammer> getScreenFactoryProvider() {
        return new ContainerLogicProgrammerConfigScreenFactoryProvider();
    }
}
