package org.cyclops.integrateddynamics.core.recipe.type;

import org.cyclops.cyclopscore.config.extendedconfig.RecipeConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link RecipeNbtClear}.
 * @author rubensworks
 */
public class RecipeSerializerNbtClearConfig extends RecipeConfigCommon<RecipeNbtClear, IntegratedDynamics> {

    public RecipeSerializerNbtClearConfig() {
        super(IntegratedDynamics._instance,
                "crafting_special_nbt_clear",
                eConfig -> RecipeSerializerNbtClear.SERIALIZER);
    }

}
