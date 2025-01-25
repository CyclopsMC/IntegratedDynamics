package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link ContainerMechanicalDryingBasin}.
 * @author rubensworks
 */
public class ContainerMechanicalDryingBasinConfig extends GuiConfigCommon<ContainerMechanicalDryingBasin, IModBase> {

    public ContainerMechanicalDryingBasinConfig() {
        super(IntegratedDynamics._instance,
                "mechanical_drying_basin",
                eConfig -> new MenuType<>(ContainerMechanicalDryingBasin::new, FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerMechanicalDryingBasin> getScreenFactoryProvider() {
        return new ContainerMechanicalDryingBasinConfigScreenFactoryProvider();
    }
}
