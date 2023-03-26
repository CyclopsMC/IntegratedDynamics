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
import org.cyclops.integrateddynamics.api.part.read.IPartStateReader;
import org.cyclops.integrateddynamics.api.part.read.IPartTypeReader;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenPartReader;

/**
 * Config for {@link ContainerPartReader}.
 * @author rubensworks
 */
public class ContainerPartReaderConfig extends GuiConfig<ContainerPartReader<?, ?>> {

    public ContainerPartReaderConfig() {
        super(IntegratedDynamics._instance,
                "part_reader",
                eConfig -> new ContainerTypeData<>(ContainerPartReader::new, FeatureFlags.VANILLA_SET));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & MenuAccess<ContainerPartReader<?, ?>>> MenuScreens.ScreenConstructor<ContainerPartReader<?, ?>, U> getScreenFactory() {
        // Due to our use of generics, we have to delegate to a separate function.
        return new ScreenFactorySafe<>((MenuScreens.ScreenConstructor) createScreenFactory());
    }

    @OnlyIn(Dist.CLIENT)
    protected static <P extends IPartTypeReader<P, S>, S extends IPartStateReader<P>> MenuScreens.ScreenConstructor<ContainerPartReader<P, S>, ContainerScreenPartReader<P, S>> createScreenFactory() {
        return ContainerScreenPartReader::new;
    }

}
