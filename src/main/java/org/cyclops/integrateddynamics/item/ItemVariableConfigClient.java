package org.cyclops.integrateddynamics.item;

import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import org.cyclops.cyclopscore.config.extendedconfig.ItemClientConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.client.render.blockentity.SpecialModelRendererVariableOverlay;
import org.cyclops.integrateddynamics.core.client.model.ModelLoaderVariableOverlays;
import org.cyclops.integrateddynamics.core.client.model.ItemModelVariableOverlay;

import java.util.List;

/**
 * @author rubensworks
 */
public class ItemVariableConfigClient extends ItemClientConfig<IntegratedDynamics> {
    private List<ResourceLocation> subModels = null;

    public ItemVariableConfigClient(ItemConfigCommon<IntegratedDynamics> itemConfig) {
        super(itemConfig);
        itemConfig.getMod().getModEventBus().register(this);
        itemConfig.getMod().getModEventBus().addListener((RegisterSpecialModelRendererEvent event) -> event.register(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "variable_overlay_special"), SpecialModelRendererVariableOverlay.Unbaked.MAP_CODEC));
        itemConfig.getMod().getModEventBus().addListener((RegisterItemModelsEvent event) -> event.register(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "variable_overlays"), ItemModelVariableOverlay.Unbaked.MAP_CODEC));
    }

    @SubscribeEvent
    public void onModelLoading(ModelEvent.RegisterLoaders event) {
        subModels = Lists.newArrayList();
        event.register(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "variable_overlays_loader"), new ModelLoaderVariableOverlays(subModels));
    }

    @SubscribeEvent
    public void onModelLoading(ModelEvent.RegisterAdditional event) {
        for (ResourceLocation subModel : subModels) {
            event.register(subModel);
        }
    }
}
