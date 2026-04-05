package org.cyclops.integrateddynamics.core.part.aspect;

import net.minecraft.client.color.item.Constant;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.resources.Identifier;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRegistryClient;

import java.util.*;

/**
 * @author rubensworks
 */
public class AspectRegistryClient implements IAspectRegistryClient {

    private Map<IAspect, ItemModel.Unbaked> aspectModels = new IdentityHashMap<>();;

    @Override
    public void registerAspectModel(IAspect aspect, Identifier modelLocation) {
        aspectModels.put(aspect, new CuboidItemModelWrapper.Unbaked(modelLocation, java.util.Optional.empty(), List.of(new Constant(-1))));
    }

    @Override
    public ItemModel.Unbaked getAspectModel(IAspect aspect) {
        return aspectModels.get(aspect);
    }

    @Override
    public Collection<ItemModel.Unbaked> getAspectModels() {
        return Collections.unmodifiableCollection(aspectModels.values());
    }

}
