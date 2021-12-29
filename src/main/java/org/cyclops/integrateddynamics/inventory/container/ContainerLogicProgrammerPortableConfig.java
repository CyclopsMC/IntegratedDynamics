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
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerPortable;

/**
 * Config for {@link ContainerLogicProgrammerPortable}.
 * @author rubensworks
 */
public class ContainerLogicProgrammerPortableConfig extends GuiConfig<ContainerLogicProgrammerPortable> {

    public ContainerLogicProgrammerPortableConfig() {
        super(IntegratedDynamics._instance,
                "logic_programmer_portable",
                eConfig -> new ContainerTypeData<>(ContainerLogicProgrammerPortable::new));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & MenuAccess<ContainerLogicProgrammerPortable>> MenuScreens.ScreenConstructor<ContainerLogicProgrammerPortable, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenLogicProgrammerPortable::new);
    }

}
