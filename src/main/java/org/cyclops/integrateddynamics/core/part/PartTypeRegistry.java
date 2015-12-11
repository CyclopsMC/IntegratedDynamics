package org.cyclops.integrateddynamics.core.part;

import com.google.common.collect.Maps;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.IPartTypeRegistry;

import java.util.Collection;
import java.util.Map;

/**
 * Registry for {@link IPartType}.
 * @author rubensworks
 */
public final class PartTypeRegistry implements IPartTypeRegistry {

    private static PartTypeRegistry INSTANCE = new PartTypeRegistry();

    private Map<String, IPartType> partTypes = Maps.newHashMap();

    private PartTypeRegistry() {

    }

    /**
     * @return The unique instance.
     */
    public static PartTypeRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public <P extends IPartType<P, S>, S extends IPartState<P>> P register(P partType) {
        if(partTypes.containsKey(partType.getName())) {
            throw new DuplicatePartTypeException(String.format("Tried to register a part type %s with name %s while " +
                    "the registry already container %s for that name.", partType, partType.getName(),
                    partTypes.get(partType.getName())));
        }
        partTypes.put(partType.getName(), partType);
        return partType;
    }

    @Override
    public Collection<IPartType> getPartTypes() {
        return partTypes.values();
    }

    @Override
    public IPartType getPartType(String partName) {
        return partTypes.get(partName);
    }

    @Override
    public void onInit(Step initStep) {
        for(IPartType partType : getPartTypes()) {
            partType.onInit(initStep);
        }
    }

    public static class DuplicatePartTypeException extends RuntimeException {

        public DuplicatePartTypeException(String msg) {
            super(msg);
        }

    }

}
