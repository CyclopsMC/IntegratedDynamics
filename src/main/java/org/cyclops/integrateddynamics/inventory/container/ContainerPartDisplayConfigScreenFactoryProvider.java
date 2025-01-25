package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenPartDisplay;
import org.cyclops.integrateddynamics.core.part.panel.PartTypePanelVariableDriven;

/**
 * @author rubensworks
 */
public class ContainerPartDisplayConfigScreenFactoryProvider extends GuiConfigScreenFactoryProvider<ContainerPartPanelVariableDriven> {
    @Override
    public <U extends Screen & MenuAccess<ContainerPartPanelVariableDriven>> MenuScreens.ScreenConstructor<ContainerPartPanelVariableDriven, U> getScreenFactory() {
        // Due to our use of generics, we have to delegate to a separate function.
        return new ScreenFactorySafe<>((MenuScreens.ScreenConstructor) createScreenFactory());
    }

    protected static <P extends PartTypePanelVariableDriven<P, S>, S extends PartTypePanelVariableDriven.State<P, S>> MenuScreens.ScreenConstructor<ContainerPartPanelVariableDriven<P, S>, ContainerScreenPartDisplay<P, S>> createScreenFactory() {
        return ContainerScreenPartDisplay::new;
    }
}
