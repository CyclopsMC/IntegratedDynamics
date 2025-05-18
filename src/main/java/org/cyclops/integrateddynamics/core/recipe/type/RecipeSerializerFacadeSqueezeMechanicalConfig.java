package org.cyclops.integrateddynamics.core.recipe.type;


import org.cyclops.cyclopscore.config.extendedconfig.RecipeConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;


/**
 * Config for the Mechanical Facade Squeeze recipe serializer.
 * @author kirjorjos
 *
 */
public class RecipeSerializerFacadeSqueezeMechanicalConfig extends RecipeConfig<RecipeFacadeSqueezeMechanical> {

    public RecipeSerializerFacadeSqueezeMechanicalConfig() {
        super(
                IntegratedDynamics._instance,
                "facade_squeeze_mechanical",
                eConfig -> new RecipeSerializerFacadeSqueezeMechanical()
        );
    }

}
