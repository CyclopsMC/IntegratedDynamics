package org.cyclops.integrateddynamics.core.client.model;

import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelBaked;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProvider;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Baked item facadeModel overlays for variables.
 * @author rubensworks
 */
public class ItemModelVariableOverlays implements IVariableModelBaked {

    private final Map<IVariableModelProvider<?>, IVariableModelProvider.BakedModelProvider> subModels = Maps.newHashMap();

    @Override
    public <B extends IVariableModelProvider.BakedModelProvider> void setSubModels(IVariableModelProvider<B> provider, B subModels) {
        this.subModels.put(provider, subModels);
    }

    @Override
    public <B extends IVariableModelProvider.BakedModelProvider> B getSubModels(IVariableModelProvider<B> provider) {
        return (B) this.subModels.get(provider);
    }

    @Nullable
    public ItemModel getModelForItem(ItemStack itemStack, Level world) {
        // Add variable type overlay
        IVariableFacade variableFacade = RegistryEntries.ITEM_VARIABLE.get().getVariableFacade(ValueDeseralizationContext.of(world == null ? Minecraft.getInstance().level : world), itemStack);
        return variableFacade.getClient().getItemModelOverlay(this);
    }
}
