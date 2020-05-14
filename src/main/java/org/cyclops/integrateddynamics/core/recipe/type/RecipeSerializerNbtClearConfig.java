package org.cyclops.integrateddynamics.core.recipe.type;

import org.cyclops.cyclopscore.config.extendedconfig.RecipeConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link RecipeNbtClear}.
 * @author rubensworks
 */
public class RecipeSerializerNbtClearConfig extends RecipeConfig<RecipeNbtClear> {

    public RecipeSerializerNbtClearConfig() {
        super(IntegratedDynamics._instance,
                "crafting_special_nbt_clear",
                eConfig -> new RecipeSerializerNbtClear());
    }

}
