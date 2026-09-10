package org.cyclops.integrateddynamics.item;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.cyclops.cyclopscore.config.extendedconfig.ItemClientConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;

/**
 * Client-side config for {@link ItemWrench}.
 * @author rubensworks
 */
public class ItemWrenchClientConfig extends ItemClientConfig<IntegratedDynamics> {

    public ItemWrenchClientConfig(ItemConfigCommon<IntegratedDynamics> itemConfig) {
        super(itemConfig);
        itemConfig.getMod().getModEventBus().addListener(this::onClientSetup);
    }

    public void onClientSetup(FMLClientSetupEvent event) {
        // Show the active wrench mode on the item, by picking a model variant for it
        event.enqueueWork(() -> ItemProperties.register(getItemConfig().getInstance(),
                ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "wrench_mode"),
                (itemStack, level, entity, seed) -> {
                    Item item = itemStack.getItem();
                    // Values are divided by ten to fit the clamped 0 to 1 range that item properties have
                    return item instanceof ItemWrench itemWrench ? itemWrench.getMode(itemStack).ordinal() / 10F : 0F;
                }));
    }

}
