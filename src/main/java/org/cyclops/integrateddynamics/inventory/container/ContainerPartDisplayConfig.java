package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.world.flag.FeatureFlags;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link ContainerPartPanelVariableDriven}.
 * @author rubensworks
 */
public class ContainerPartDisplayConfig extends GuiConfigCommon<ContainerPartPanelVariableDriven, IModBase> {

    public ContainerPartDisplayConfig() {
        super(IntegratedDynamics._instance,
                "part_display",
                eConfig -> new ContainerTypeData<>(ContainerPartPanelVariableDriven::new, FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerPartPanelVariableDriven> getScreenFactoryProvider() {
        return new ContainerPartDisplayConfigScreenFactoryProvider();
    }
}
