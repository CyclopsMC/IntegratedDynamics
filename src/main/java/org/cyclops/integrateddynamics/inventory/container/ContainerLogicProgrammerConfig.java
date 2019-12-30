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
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammer;

/**
 * Config for {@link ContainerLogicProgrammer}.
 * @author rubensworks
 */
public class ContainerLogicProgrammerConfig extends GuiConfig<ContainerLogicProgrammer> {

    public ContainerLogicProgrammerConfig() {
        super(IntegratedDynamics._instance,
                "logic_programmer",
                eConfig -> new ContainerType<>(ContainerLogicProgrammer::new));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & IHasContainer<ContainerLogicProgrammer>> ScreenManager.IScreenFactory<ContainerLogicProgrammer, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenLogicProgrammer::new);
    }

}
