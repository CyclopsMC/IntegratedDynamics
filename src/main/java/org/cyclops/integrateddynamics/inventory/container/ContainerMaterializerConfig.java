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
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenMaterializer;

/**
 * Config for {@link ContainerMaterializer}.
 * @author rubensworks
 */
public class ContainerMaterializerConfig extends GuiConfig<ContainerMaterializer> {

    public ContainerMaterializerConfig() {
        super(IntegratedDynamics._instance,
                "materializer",
                eConfig -> new ContainerType<>(ContainerMaterializer::new));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & IHasContainer<ContainerMaterializer>> ScreenManager.IScreenFactory<ContainerMaterializer, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenMaterializer::new);
    }

}
