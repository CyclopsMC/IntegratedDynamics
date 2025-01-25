package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.integrateddynamics.api.part.read.IPartStateReader;
import org.cyclops.integrateddynamics.api.part.read.IPartTypeReader;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenPartReader;

/**
 * @author rubensworks
 */
public class ContainerPartReaderConfigScreenFactoryProvider extends GuiConfigScreenFactoryProvider<ContainerPartReader<?, ?>> {
    @Override
    public <U extends Screen & MenuAccess<ContainerPartReader<?, ?>>> MenuScreens.ScreenConstructor<ContainerPartReader<?, ?>, U> getScreenFactory() {
        // Due to our use of generics, we have to delegate to a separate function.
        return new ScreenFactorySafe<>((MenuScreens.ScreenConstructor) createScreenFactory());
    }

    protected static <P extends IPartTypeReader<P, S>, S extends IPartStateReader<P>> MenuScreens.ScreenConstructor<ContainerPartReader<P, S>, ContainerScreenPartReader<P, S>> createScreenFactory() {
        return ContainerScreenPartReader::new;
    }
}
