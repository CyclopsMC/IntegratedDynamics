package org.cyclops.integrateddynamics.api.part.aspect;

import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;

/**
 * @author rubensworks
 */
public interface IAspectRegistryClient {

    /**
     * Register a facadeModel resource location for the given aspect.
     * @param aspect The aspect.
     * @param modelLocation The facadeModel resource location.
     */
    public void registerAspectModel(IAspect aspect, ResourceLocation modelLocation);

    /**
     * Get the facadeModel resource location of the given aspect.
     *
     * @param aspect The aspect.
     * @return The unbaked facadeModel.
     */
    public ItemModel.Unbaked getAspectModel(IAspect aspect);

    /**
     * Get all registered facadeModel resource locations for the aspects.
     * @return All facadeModel resource locations.
     */
    public Collection<ItemModel.Unbaked> getAspectModels();

}
