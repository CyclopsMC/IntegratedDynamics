package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammer;

/**
 * Config for {@link ContainerLogicProgrammer}.
 * @author rubensworks
 */
public class ContainerLogicProgrammerConfig extends GuiConfig<ContainerLogicProgrammer> {

    public ContainerLogicProgrammerConfig() {
        super(IntegratedDynamics._instance,
                "logic_programmer",
                eConfig -> new MenuType<>(ContainerLogicProgrammer::new, FeatureFlags.VANILLA_SET));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & MenuAccess<ContainerLogicProgrammer>> MenuScreens.ScreenConstructor<ContainerLogicProgrammer, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenLogicProgrammer::new);
    }

}
