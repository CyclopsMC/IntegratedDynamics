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
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenVariablestore;

/**
 * Config for {@link ContainerVariablestore}.
 * @author rubensworks
 */
public class ContainerVariablestoreConfig extends GuiConfig<ContainerVariablestore> {

    public ContainerVariablestoreConfig() {
        super(IntegratedDynamics._instance,
                "variablestore",
                eConfig -> new ContainerType<>(ContainerVariablestore::new));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & IHasContainer<ContainerVariablestore>> ScreenManager.IScreenFactory<ContainerVariablestore, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenVariablestore::new);
    }

}
