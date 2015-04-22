package org.cyclops.integrateddynamics.core.part;

import com.google.common.collect.Maps;
import lombok.Data;

import java.util.Collection;
import java.util.Map;

/**
 * Enumeration for the types of {@link IPartType}.
 * @author rubensworks
 */
@Data
public final class EnumPartType {

    private static final Map<String, EnumPartType> INSTANCES = Maps.newHashMap();

    private final String name;
    private final IPartType part;

    private EnumPartType(String name, IPartType part) {
        this.name = name;
        this.part = part;
        INSTANCES.put(name, this);
    }

    @Override
    public String toString() {
        return this.name;
    }

    /**
     * Create a new instance.
     * @param name The unique name.
     * @param part The part instance for this type.
     * @return The type instance.
     */
    public static EnumPartType create(String name, IPartType part) {
        return new EnumPartType(name, part);
    }

    /**
     * Get an type instance by name.
     * @param name The name.
     * @return The instance by name.
     */
    public static EnumPartType getInstance(String name) {
        return INSTANCES.get(name);
    }

    /**
     * @return All registered part types.
     */
    public static Collection<EnumPartType> getAll() {
        return INSTANCES.values();
    }

}
