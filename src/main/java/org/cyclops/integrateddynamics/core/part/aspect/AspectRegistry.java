package org.cyclops.integrateddynamics.core.part.aspect;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.cyclops.integrateddynamics.core.part.IPartType;

import java.util.Map;
import java.util.Set;

/**
 * Registry for {@link org.cyclops.integrateddynamics.core.part.aspect.IAspect}.
 * @author rubensworks
 */
public final class AspectRegistry implements IAspectRegistry {

    private static AspectRegistry INSTANCE = new AspectRegistry();

    private Map<IPartType, Set<IAspect>> partAspects = Maps.newHashMap();

    private AspectRegistry() {

    }

    /**
     * @return The unique instance.
     */
    public static AspectRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public void register(IPartType partType, IAspect aspect) {
        Set<IAspect> aspects = partAspects.get(partType);
        if(aspects == null) {
            aspects = Sets.newHashSet();
            partAspects.put(partType, aspects);
        }
        aspects.add(aspect);
    }

    @Override
    public void register(IPartType partType, Set<IAspect> aspects) {
        for(IAspect aspect : aspects) {
            register(partType, aspect);
        }
    }

    @Override
    public Set<IAspect> getAspects(IPartType partType) {
        return partAspects.get(partType);
    }

}
