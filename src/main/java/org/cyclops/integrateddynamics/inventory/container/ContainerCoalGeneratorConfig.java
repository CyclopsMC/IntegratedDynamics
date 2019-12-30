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
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenCoalGenerator;

/**
 * Config for {@link ContainerCoalGenerator}.
 * @author rubensworks
 */
public class ContainerCoalGeneratorConfig extends GuiConfig<ContainerCoalGenerator> {

    public ContainerCoalGeneratorConfig() {
        super(IntegratedDynamics._instance,
                "coal_generator",
                eConfig -> new ContainerType<>(ContainerCoalGenerator::new));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & IHasContainer<ContainerCoalGenerator>> ScreenManager.IScreenFactory<ContainerCoalGenerator, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenCoalGenerator::new);
    }

}
