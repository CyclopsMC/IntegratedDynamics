package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link ContainerMaterializer}.
 * @author rubensworks
 */
public class ContainerMaterializerConfig extends GuiConfigCommon<ContainerMaterializer, IModBase> {

    public ContainerMaterializerConfig() {
        super(IntegratedDynamics._instance,
                "materializer",
                eConfig -> new MenuType<>(ContainerMaterializer::new, FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerMaterializer> getScreenFactoryProvider() {
        return new ContainerMaterializerConfigScreenFactoryProvider();
    }
}
