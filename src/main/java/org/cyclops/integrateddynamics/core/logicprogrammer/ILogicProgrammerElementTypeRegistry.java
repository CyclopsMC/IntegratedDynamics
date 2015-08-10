package org.cyclops.integrateddynamics.core.logicprogrammer;

import org.cyclops.cyclopscore.init.IRegistry;

import java.util.List;

/**
 * Registry for {@link ILogicProgrammerElementType}.
 * @author rubensworks
 */
public interface ILogicProgrammerElementTypeRegistry extends IRegistry {

    /**
     * Register a new type.
     * @param type The type to register.
     * @param <E> The type of type.
     * @return The registered type
     */
    public <E extends ILogicProgrammerElementType> E addType(E type);

    /**
     * @return All registered types.
     */
    public List<ILogicProgrammerElementType> getTypes();

    /**
     * Get the type by name.
     * @param name The name.
     * @return The type.
     */
    public ILogicProgrammerElementType getType(String name);

}
