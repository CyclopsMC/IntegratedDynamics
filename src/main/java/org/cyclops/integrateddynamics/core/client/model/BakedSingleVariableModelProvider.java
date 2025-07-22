package org.cyclops.integrateddynamics.core.client.model;

import lombok.Data;
import net.minecraft.client.renderer.item.ItemModel;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProvider;

/**
 * A baked facadeModel provider that maps keys to baked models.
 * @author rubensworks
 */
@Data
public class BakedSingleVariableModelProvider implements IVariableModelProvider.BakedModelProvider {
    private final ItemModel bakedModel;
}
