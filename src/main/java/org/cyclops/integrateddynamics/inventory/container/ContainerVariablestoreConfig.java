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
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenVariablestore;

/**
 * Config for {@link ContainerVariablestore}.
 * @author rubensworks
 */
public class ContainerVariablestoreConfig extends GuiConfig<ContainerVariablestore> {

    public ContainerVariablestoreConfig() {
        super(IntegratedDynamics._instance,
                "variablestore",
                eConfig -> new MenuType<>(ContainerVariablestore::new));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & MenuAccess<ContainerVariablestore>> MenuScreens.ScreenConstructor<ContainerVariablestore, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenVariablestore::new);
    }

}
