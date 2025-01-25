package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.world.flag.FeatureFlags;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link ContainerPartWriter}.
 * @author rubensworks
 */
public class ContainerPartWriterConfig extends GuiConfigCommon<ContainerPartWriter, IModBase> {

    public ContainerPartWriterConfig() {
        super(IntegratedDynamics._instance,
                "part_writer",
                eConfig -> new ContainerTypeData<>(ContainerPartWriter::new, FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerPartWriter> getScreenFactoryProvider() {
        return new ContainerPartWriterConfigScreenFactoryProvider();
    }
}
