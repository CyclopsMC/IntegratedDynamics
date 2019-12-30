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
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenMechanicalSqueezer;

/**
 * Config for {@link ContainerMechanicalSqueezer}.
 * @author rubensworks
 */
public class ContainerMechanicalSqueezerConfig extends GuiConfig<ContainerMechanicalSqueezer> {

    public ContainerMechanicalSqueezerConfig() {
        super(IntegratedDynamics._instance,
                "mechanical_squeezer",
                eConfig -> new ContainerType<>(ContainerMechanicalSqueezer::new));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & IHasContainer<ContainerMechanicalSqueezer>> ScreenManager.IScreenFactory<ContainerMechanicalSqueezer, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenMechanicalSqueezer::new);
    }

}
