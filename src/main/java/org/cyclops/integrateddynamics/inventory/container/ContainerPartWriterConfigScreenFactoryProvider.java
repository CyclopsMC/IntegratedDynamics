package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenPartWriter;

/**
 * @author rubensworks
 */
public class ContainerPartWriterConfigScreenFactoryProvider extends GuiConfigScreenFactoryProvider<ContainerPartWriter> {
    @Override
    public <U extends Screen & MenuAccess<ContainerPartWriter>> MenuScreens.ScreenConstructor<ContainerPartWriter, U> getScreenFactory() {
        // Due to our use of generics, we have to delegate to a separate function.
        return new ScreenFactorySafe<>((MenuScreens.ScreenConstructor) createScreenFactory());
    }

    protected static <P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>> MenuScreens.ScreenConstructor<ContainerPartWriter<P, S>, ContainerScreenPartWriter<P, S>> createScreenFactory() {
        return ContainerScreenPartWriter::new;
    }
}
