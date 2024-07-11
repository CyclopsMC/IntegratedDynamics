package org.cyclops.integrateddynamics.item;

import com.google.common.collect.Lists;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.capability.variablefacade.VariableFacadeHolderDefault;
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
        IntegratedDynamics._instance.getModEventBus().register(this);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onModelLoading(ModelEvent.RegisterGeometryLoaders event) {
        subModels = Lists.newArrayList();
        event.register(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "variable"), new ModelLoaderVariable(subModels));
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onModelLoading(ModelEvent.RegisterAdditional event) {
        for (ResourceLocation subModel : subModels) {
            event.register(ModelResourceLocation.standalone(subModel));
        }
    }

    @SubscribeEvent
    protected void registerCapability(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.VariableFacade.ITEM, (stack, context) -> new VariableFacadeHolderDefault(stack), getInstance());
    }
}
