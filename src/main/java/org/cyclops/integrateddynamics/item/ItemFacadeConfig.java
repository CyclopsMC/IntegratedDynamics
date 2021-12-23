package org.cyclops.integrateddynamics.item;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the facade.
 * @author rubensworks
 */
public class ItemFacadeConfig extends ItemConfig {

    public ItemFacadeConfig() {
        super(
                IntegratedDynamics._instance,
                "facade",
                eConfig -> new ItemFacade(new Item.Properties()
                        .tab(IntegratedDynamics._instance.getDefaultItemGroup()))
        );
        FMLJavaModLoadingContext.get().getModEventBus().register(this);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onModLoaded(FMLLoadCompleteEvent event) {
        Minecraft.getInstance().getItemColors().register(new ItemFacade.ItemColor(), getInstance());
    }

}
