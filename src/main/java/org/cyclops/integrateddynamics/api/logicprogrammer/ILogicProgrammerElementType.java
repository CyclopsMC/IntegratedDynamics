package org.cyclops.integrateddynamics.api.logicprogrammer;

import net.minecraft.util.ResourceLocation;

import java.util.List;

/**
 * Indicates a type of logic programmer element.
 * This creates one or more {@link ILogicProgrammerElement}.
 * Must be registered in {@link ILogicProgrammerElementTypeRegistry}.
 * @author rubensworks
 */
public interface ILogicProgrammerElementType<E extends ILogicProgrammerElement> {

    /**
     * Get the element from the given name.
     * @param name The name, already namespaced for this type.
     * @return The element.
     */
    public E getByName(ResourceLocation name);

    /**
     * Get the name from given element, no need to namespace.
     * @param element The element.
     * @return The unique name.
     */
    public ResourceLocation getName(E element);

    /**
     * @return Unique name.
     */
    public ResourceLocation getUniqueName();

    /**
     * @return All the elements this type can have.
     */
    public List<E> createElements();

}
