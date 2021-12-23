package org.cyclops.integrateddynamics.item;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.client.render.tileentity.ItemStackTileEntityVariableRender;
import org.cyclops.integrateddynamics.core.client.model.ModelLoaderVariable;

/**
 * Config for a variable item.
 * @author rubensworks
 */
public class ItemVariableConfig extends ItemConfig {

    public ItemVariableConfig() {
        super(
                IntegratedDynamics._instance,
                "variable",
                eConfig -> new ItemVariable(new Item.Properties()
                        .tab(IntegratedDynamics._instance.getDefaultItemGroup())
                        .setISTER(() -> ItemStackTileEntityVariableRender::new))
        );
        FMLJavaModLoadingContext.get().getModEventBus().register(this);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onModelLoading(ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(new ResourceLocation(Reference.MOD_ID, "variable"), new ModelLoaderVariable());
    }

}
