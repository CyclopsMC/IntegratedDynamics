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
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenPartReader;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenPartWriter;

/**
 * Config for {@link ContainerPartReader}.
 * @author rubensworks
 */
public class ContainerPartReaderConfig extends GuiConfig<ContainerPartReader<?, ?>> {

    public ContainerPartReaderConfig() {
        super(IntegratedDynamics._instance,
                "part_reader",
                eConfig -> new ContainerTypeData<>(ContainerPartReader::new));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & IHasContainer<ContainerPartReader<?, ?>>> ScreenManager.IScreenFactory<ContainerPartReader<?, ?>, U> getScreenFactory() {
        // Due to our use of generics, we have to delegate to a separate function.
        return new ScreenFactorySafe<>((ScreenManager.IScreenFactory) createScreenFactory());
    }

    @OnlyIn(Dist.CLIENT)
    protected static <P extends IPartTypeReader<P, S>, S extends IPartStateReader<P>> ScreenManager.IScreenFactory<ContainerPartReader<P, S>, ContainerScreenPartReader<P, S>> createScreenFactory() {
        return ContainerScreenPartReader::new;
    }

}
