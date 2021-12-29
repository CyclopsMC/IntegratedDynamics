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
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenMechanicalDryingBasin;

/**
 * Config for {@link ContainerMechanicalDryingBasin}.
 * @author rubensworks
 */
public class ContainerMechanicalDryingBasinConfig extends GuiConfig<ContainerMechanicalDryingBasin> {

    public ContainerMechanicalDryingBasinConfig() {
        super(IntegratedDynamics._instance,
                "mechanical_drying_basin",
                eConfig -> new MenuType<>(ContainerMechanicalDryingBasin::new));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & MenuAccess<ContainerMechanicalDryingBasin>> MenuScreens.ScreenConstructor<ContainerMechanicalDryingBasin, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenMechanicalDryingBasin::new);
    }

}
