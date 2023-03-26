package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
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
                eConfig -> new MenuType<>(ContainerMaterializer::new, FeatureFlags.VANILLA_SET));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & MenuAccess<ContainerMaterializer>> MenuScreens.ScreenConstructor<ContainerMaterializer, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenMaterializer::new);
    }

}
