package org.cyclops.integrateddynamics.core.client.model;

import lombok.Data;
import net.minecraft.client.resources.model.IBakedModel;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProvider;

import java.util.Map;

/**
 * A baked model provider that maps keys to baked models.
 * @param <T> The key type.
 * @author rubensworks
 */
@Data
public class BakedMapVariableModelProvider<T> implements IVariableModelProvider.IBakedModelProvider {
    private final Map<T, IBakedModel> bakedModels;
}
