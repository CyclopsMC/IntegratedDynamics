package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfig;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLabeller;

/**
 * Config for {@link ContainerLabeller}.
 * @author rubensworks
 */
public class ContainerLabellerConfig extends GuiConfig<ContainerLabeller> {

    public ContainerLabellerConfig() {
        super(IntegratedDynamics._instance,
                "labeller",
                eConfig -> new ContainerTypeData<>(ContainerLabeller::new));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & MenuAccess<ContainerLabeller>> MenuScreens.ScreenConstructor<ContainerLabeller, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenLabeller::new);
    }

}
