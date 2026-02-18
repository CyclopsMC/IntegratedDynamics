package org.cyclops.integrateddynamics.core.client.model;

import net.minecraft.client.renderer.item.ItemModel;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProvider;

import java.util.Objects;

/**
 * A baked facadeModel provider that maps keys to baked models.
 * @author rubensworks
 */
public class BakedSingleVariableModelProvider implements IVariableModelProvider.BakedModelProvider {
    private final ItemModel bakedModel;

    public BakedSingleVariableModelProvider(ItemModel bakedModel) {
        this.bakedModel = bakedModel;
    }

    public ItemModel getBakedModel() {
        return bakedModel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BakedSingleVariableModelProvider that = (BakedSingleVariableModelProvider) o;
        return Objects.equals(bakedModel, that.bakedModel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bakedModel);
    }

    @Override
    public String toString() {
        return "BakedSingleVariableModelProvider(bakedModel=" + bakedModel + ")";
    }
}
