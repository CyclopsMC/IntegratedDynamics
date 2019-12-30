package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfig;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.part.read.IPartStateReader;
import org.cyclops.integrateddynamics.api.part.read.IPartTypeReader;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenPartDisplay;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenPartReader;
import org.cyclops.integrateddynamics.core.part.panel.PartTypePanelVariableDriven;

/**
 * Config for {@link ContainerPartDisplay}.
 * @author rubensworks
 */
public class ContainerPartDisplayConfig extends GuiConfig<ContainerPartDisplay> {

    public ContainerPartDisplayConfig() {
        super(IntegratedDynamics._instance,
                "part_display",
                eConfig -> new ContainerTypeData<>(ContainerPartDisplay::new));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & IHasContainer<ContainerPartDisplay>> ScreenManager.IScreenFactory<ContainerPartDisplay, U> getScreenFactory() {
        // Due to our use of generics, we have to delegate to a separate function.
        return new ScreenFactorySafe<>((ScreenManager.IScreenFactory) createScreenFactory());
    }

    @OnlyIn(Dist.CLIENT)
    protected static <P extends PartTypePanelVariableDriven<P, S>, S extends PartTypePanelVariableDriven.State<P, S>> ScreenManager.IScreenFactory<ContainerPartDisplay<P, S>, ContainerScreenPartDisplay<P, S>> createScreenFactory() {
        return ContainerScreenPartDisplay::new;
    }

}
