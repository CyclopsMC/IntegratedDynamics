package org.cyclops.integrateddynamics.core.client.model;

import net.minecraft.client.color.item.Constant;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProvider;

import java.util.List;

/**
 * Variable facadeModel provider for a single facadeModel.
 * @author rubensworks
 */
public class SingleVariableModelProvider implements IVariableModelProvider<BakedSingleVariableModelProvider> {

    private ItemModel.Unbaked modelUnbaked;

    public SingleVariableModelProvider(ResourceLocation model) {
        this.modelUnbaked = new BlockModelWrapper.Unbaked(model, List.of(new Constant(-1)));
    }

    @Override
    public BakedSingleVariableModelProvider bakeOverlayModels(ItemModel.BakingContext bakingContext) {
        return new BakedSingleVariableModelProvider(this.modelUnbaked.bake(bakingContext));
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
        this.modelUnbaked.resolveDependencies(resolver);
    }

}
