package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.world.flag.FeatureFlags;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link ContainerLabeller}.
 * @author rubensworks
 */
public class ContainerLabellerConfig extends GuiConfigCommon<ContainerLabeller, IModBase> {

    public ContainerLabellerConfig() {
        super(IntegratedDynamics._instance,
                "labeller",
                eConfig -> new ContainerTypeData<>(ContainerLabeller::new, FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerLabeller> getScreenFactoryProvider() {
        return new ContainerLabellerConfigScreenFactoryProvider();
    }
}
