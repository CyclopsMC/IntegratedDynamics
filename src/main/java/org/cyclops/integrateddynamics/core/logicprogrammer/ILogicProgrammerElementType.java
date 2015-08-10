package org.cyclops.integrateddynamics.core.logicprogrammer;

import java.util.List;

/**
 * Indicates a type of logic programmer element.
 * @author rubensworks
 */
public interface ILogicProgrammerElementType<E extends ILogicProgrammerElement> {

    /**
     * Get the element from the given name.
     * @param name The name, already namespaced for this type.
     * @return The element.
     */
    public E getByName(String name);

    /**
     * Get the name from given element, no need to namespace.
     * @param element The element.
     * @return The unique name.
     */
    public String getName(E element);

    /**
     * @return Unique name.
     */
    public String getName();

    /**
     * @return All the elements this type can have.
     */
    public List<E> createElements();

}
