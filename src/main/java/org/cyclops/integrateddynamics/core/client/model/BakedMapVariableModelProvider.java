package org.cyclops.integrateddynamics.core.client.model;

import lombok.Data;
import net.minecraft.client.renderer.item.ItemModel;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProvider;

import java.util.Map;

/**
 * A baked facadeModel provider that maps keys to baked models.
 * @param <T> The key type.
 * @author rubensworks
 */
@Data
public class BakedMapVariableModelProvider<T> implements IVariableModelProvider.BakedModelProvider {
    private final Map<T, ItemModel> bakedModels;
}
