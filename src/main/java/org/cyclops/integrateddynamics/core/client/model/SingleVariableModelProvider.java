package org.cyclops.integrateddynamics.core.client.model;

import net.minecraft.client.color.item.Constant;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.resources.Identifier;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProvider;
import org.joml.Matrix4fc;

import java.util.List;

/**
 * Variable facadeModel provider for a single facadeModel.
 * @author rubensworks
 */
public class SingleVariableModelProvider implements IVariableModelProvider<BakedSingleVariableModelProvider> {

    private ItemModel.Unbaked modelUnbaked;

    public SingleVariableModelProvider(Identifier model) {
        this.modelUnbaked = new CuboidItemModelWrapper.Unbaked(model, java.util.Optional.empty(), List.of(new Constant(-1)));
    }

    @Override
    public BakedSingleVariableModelProvider bakeOverlayModels(ItemModel.BakingContext bakingContext, Matrix4fc matrix) {
        return new BakedSingleVariableModelProvider(this.modelUnbaked.bake(bakingContext, matrix));
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
        this.modelUnbaked.resolveDependencies(resolver);
    }

}
