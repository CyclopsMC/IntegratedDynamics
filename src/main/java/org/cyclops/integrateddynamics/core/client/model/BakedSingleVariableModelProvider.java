package org.cyclops.integrateddynamics.core.client.model;

import lombok.Data;
import net.minecraft.client.renderer.model.IBakedModel;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProvider;

/**
 * A baked model provider that maps keys to baked models.
 * @author rubensworks
 */
@Data
public class BakedSingleVariableModelProvider implements IVariableModelProvider.IBakedModelProvider {
    private final IBakedModel bakedModel;
}
