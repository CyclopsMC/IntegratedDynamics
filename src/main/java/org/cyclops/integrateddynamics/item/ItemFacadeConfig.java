package org.cyclops.integrateddynamics.item;

import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
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
        if (MinecraftHelpers.isClientSide()) {
            IntegratedDynamics._instance.getModEventBus().addListener(this::onRegisterColors);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void onRegisterColors(RegisterColorHandlersEvent.Item event) {
        event.register(new ItemFacade.Color(), getInstance());
    }

}
