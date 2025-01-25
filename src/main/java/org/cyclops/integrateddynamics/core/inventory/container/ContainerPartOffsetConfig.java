package org.cyclops.integrateddynamics.core.inventory.container;

import net.minecraft.world.flag.FeatureFlags;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link ContainerPartOffset}.
 * @author rubensworks
 */
public class ContainerPartOffsetConfig extends GuiConfigCommon<ContainerPartOffset, IntegratedDynamics> {

    public ContainerPartOffsetConfig() {
        super(IntegratedDynamics._instance,
                "part_offset",
                eConfig -> new ContainerTypeData<>(ContainerPartOffset::new, FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerPartOffset> getScreenFactoryProvider() {
        return new ContainerPartOffsetConfigScreenFactoryProvider();
    }
}
