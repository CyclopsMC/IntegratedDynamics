package org.cyclops.integrateddynamics.item;

import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.client.model.ModelLoaderVariable;

import java.util.List;

/**
 * Config for a variable item.
 * @author rubensworks
 */
public class ItemVariableConfig extends ItemConfig {

    private List<ResourceLocation> subModels = null;

    public ItemVariableConfig() {
        super(
                IntegratedDynamics._instance,
                "variable",
                eConfig -> new ItemVariable(new Item.Properties())
        );
        FMLJavaModLoadingContext.get().getModEventBus().register(this);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onModelLoading(ModelEvent.RegisterGeometryLoaders event) {
        subModels = Lists.newArrayList();
        event.register("variable", new ModelLoaderVariable(subModels));
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onModelLoading(ModelEvent.RegisterAdditional event) {
        for (ResourceLocation subModel : subModels) {
            event.register(subModel);
        }
    }
}
