package org.cyclops.integrateddynamics.item;

import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import org.cyclops.cyclopscore.config.extendedconfig.ItemClientConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.client.render.blockentity.ItemStackBlockEntityVariableRender;
import org.cyclops.integrateddynamics.core.client.model.ModelLoaderVariable;

import java.util.List;

/**
 * @author rubensworks
 */
public class ItemVariableConfigClient extends ItemClientConfig<IntegratedDynamics> {
    private List<ResourceLocation> subModels = null;

    public ItemVariableConfigClient(ItemConfigCommon<IntegratedDynamics> itemConfig) {
        super(itemConfig);
        itemConfig.getMod().getModEventBus().register(this);
        itemConfig.getMod().getModEventBus().addListener((RegisterSpecialModelRendererEvent event) -> event.register(itemConfig.getResourceKey().location(), ItemStackBlockEntityVariableRender.Unbaked.MAP_CODEC));
    }

    @SubscribeEvent
    public void onModelLoading(ModelEvent.RegisterLoaders event) {
        subModels = Lists.newArrayList();
        event.register(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "variable"), new ModelLoaderVariable(subModels));
    }

    @SubscribeEvent
    public void onModelLoading(ModelEvent.RegisterAdditional event) {
        for (ResourceLocation subModel : subModels) {
            event.register(subModel);
        }
    }
}
