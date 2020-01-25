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
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenPartDisplay;
import org.cyclops.integrateddynamics.core.part.panel.PartTypePanelVariableDriven;

/**
 * Config for {@link ContainerPartPanelVariableDriven}.
 * @author rubensworks
 */
public class ContainerPartDisplayConfig extends GuiConfig<ContainerPartPanelVariableDriven> {

    public ContainerPartDisplayConfig() {
        super(IntegratedDynamics._instance,
                "part_display",
                eConfig -> new ContainerTypeData<>(ContainerPartPanelVariableDriven::new));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & IHasContainer<ContainerPartPanelVariableDriven>> ScreenManager.IScreenFactory<ContainerPartPanelVariableDriven, U> getScreenFactory() {
        // Due to our use of generics, we have to delegate to a separate function.
        return new ScreenFactorySafe<>((ScreenManager.IScreenFactory) createScreenFactory());
    }

    @OnlyIn(Dist.CLIENT)
    protected static <P extends PartTypePanelVariableDriven<P, S>, S extends PartTypePanelVariableDriven.State<P, S>> ScreenManager.IScreenFactory<ContainerPartPanelVariableDriven<P, S>, ContainerScreenPartDisplay<P, S>> createScreenFactory() {
        return ContainerScreenPartDisplay::new;
    }

}
