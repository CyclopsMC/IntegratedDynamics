package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.flag.FeatureFlags;
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
                eConfig -> new ContainerTypeData<>(ContainerPartPanelVariableDriven::new, FeatureFlags.VANILLA_SET));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & MenuAccess<ContainerPartPanelVariableDriven>> MenuScreens.ScreenConstructor<ContainerPartPanelVariableDriven, U> getScreenFactory() {
        // Due to our use of generics, we have to delegate to a separate function.
        return new ScreenFactorySafe<>((MenuScreens.ScreenConstructor) createScreenFactory());
    }

    @OnlyIn(Dist.CLIENT)
    protected static <P extends PartTypePanelVariableDriven<P, S>, S extends PartTypePanelVariableDriven.State<P, S>> MenuScreens.ScreenConstructor<ContainerPartPanelVariableDriven<P, S>, ContainerScreenPartDisplay<P, S>> createScreenFactory() {
        return ContainerScreenPartDisplay::new;
    }

}
