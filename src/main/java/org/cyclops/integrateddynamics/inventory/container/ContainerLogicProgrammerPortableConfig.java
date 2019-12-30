package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
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
    public <U extends Screen & IHasContainer<ContainerLogicProgrammerPortable>> ScreenManager.IScreenFactory<ContainerLogicProgrammerPortable, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenLogicProgrammerPortable::new);
    }

}
