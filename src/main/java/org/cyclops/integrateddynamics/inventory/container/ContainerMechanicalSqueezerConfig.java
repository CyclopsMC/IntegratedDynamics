package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link ContainerMechanicalSqueezer}.
 * @author rubensworks
 */
public class ContainerMechanicalSqueezerConfig extends GuiConfigCommon<ContainerMechanicalSqueezer, IModBase> {

    public ContainerMechanicalSqueezerConfig() {
        super(IntegratedDynamics._instance,
                "mechanical_squeezer",
                eConfig -> new MenuType<>(ContainerMechanicalSqueezer::new, FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerMechanicalSqueezer> getScreenFactoryProvider() {
        return new ContainerMechanicalSqueezerConfigScreenFactoryProvider();
    }
}
