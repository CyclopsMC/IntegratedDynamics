package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.inventory.container.ContainerType;
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
                eConfig -> new ContainerType<>(ContainerMechanicalDryingBasin::new));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & IHasContainer<ContainerMechanicalDryingBasin>> ScreenManager.IScreenFactory<ContainerMechanicalDryingBasin, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenMechanicalDryingBasin::new);
    }

}
