package org.cyclops.integrateddynamics.core.client.model;

import net.minecraft.client.renderer.item.ItemModel;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProvider;

import java.util.Map;
import java.util.Objects;

/**
 * A baked facadeModel provider that maps keys to baked models.
 * @param <T> The key type.
 * @author rubensworks
 */
public class BakedMapVariableModelProvider<T> implements IVariableModelProvider.BakedModelProvider {
    private final Map<T, ItemModel> bakedModels;

    public BakedMapVariableModelProvider(Map<T, ItemModel> bakedModels) {
        this.bakedModels = bakedModels;
    }

    public Map<T, ItemModel> getBakedModels() {
        return bakedModels;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BakedMapVariableModelProvider<?> that = (BakedMapVariableModelProvider<?>) o;
        return Objects.equals(bakedModels, that.bakedModels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bakedModels);
    }

    @Override
    public String toString() {
        return "BakedMapVariableModelProvider(bakedModels=" + bakedModels + ")";
    }
}
