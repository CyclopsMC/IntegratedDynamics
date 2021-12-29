package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenMechanicalSqueezer;

/**
 * Config for {@link ContainerMechanicalSqueezer}.
 * @author rubensworks
 */
public class ContainerMechanicalSqueezerConfig extends GuiConfig<ContainerMechanicalSqueezer> {

    public ContainerMechanicalSqueezerConfig() {
        super(IntegratedDynamics._instance,
                "mechanical_squeezer",
                eConfig -> new MenuType<>(ContainerMechanicalSqueezer::new));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & MenuAccess<ContainerMechanicalSqueezer>> MenuScreens.ScreenConstructor<ContainerMechanicalSqueezer, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenMechanicalSqueezer::new);
    }

}
