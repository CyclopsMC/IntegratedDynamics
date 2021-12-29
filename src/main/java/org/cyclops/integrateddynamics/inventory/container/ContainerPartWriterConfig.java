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
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenPartWriter;

/**
 * Config for {@link ContainerPartWriter}.
 * @author rubensworks
 */
public class ContainerPartWriterConfig extends GuiConfig<ContainerPartWriter> {

    public ContainerPartWriterConfig() {
        super(IntegratedDynamics._instance,
                "part_writer",
                eConfig -> new ContainerTypeData<>(ContainerPartWriter::new));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & MenuAccess<ContainerPartWriter>> MenuScreens.ScreenConstructor<ContainerPartWriter, U> getScreenFactory() {
        // Due to our use of generics, we have to delegate to a separate function.
        return new ScreenFactorySafe<>((MenuScreens.ScreenConstructor) createScreenFactory());
    }

    @OnlyIn(Dist.CLIENT)
    protected static <P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>> MenuScreens.ScreenConstructor<ContainerPartWriter<P, S>, ContainerScreenPartWriter<P, S>> createScreenFactory() {
        return ContainerScreenPartWriter::new;
    }

}
