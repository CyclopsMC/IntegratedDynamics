package org.cyclops.integrateddynamics.item;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
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
                eConfig -> new ItemFacade(new Item.Properties())
        );
        IntegratedDynamics._instance.getModEventBus().register(this);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onModLoaded(FMLLoadCompleteEvent event) {
        Minecraft.getInstance().getItemColors().register(new ItemFacade.Color(), getInstance());
    }

}
