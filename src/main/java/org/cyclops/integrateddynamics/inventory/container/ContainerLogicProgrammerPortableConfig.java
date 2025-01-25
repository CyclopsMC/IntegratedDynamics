package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.world.flag.FeatureFlags;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link ContainerLogicProgrammerPortable}.
 * @author rubensworks
 */
public class ContainerLogicProgrammerPortableConfig extends GuiConfigCommon<ContainerLogicProgrammerPortable, IModBase> {

    public ContainerLogicProgrammerPortableConfig() {
        super(IntegratedDynamics._instance,
                "logic_programmer_portable",
                eConfig -> new ContainerTypeData<>(ContainerLogicProgrammerPortable::new, FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerLogicProgrammerPortable> getScreenFactoryProvider() {
        return new ContainerLogicProgrammerPortableConfigScreenFactoryProvider();
    }
}
