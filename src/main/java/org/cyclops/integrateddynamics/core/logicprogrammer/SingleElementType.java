package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.google.common.collect.ImmutableList;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElement;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementType;

import java.util.List;

/**
 * Element type that provides exactly one element.
 * @author rubensworks
 */
public class SingleElementType<E extends ILogicProgrammerElement> implements ILogicProgrammerElementType<E> {

    private final ILogicProgrammerElementConstructor<E> constructor;
    private final String id;

    public SingleElementType(ILogicProgrammerElementConstructor<E> constructor, String id) {
        this.constructor = constructor;
        this.id = id;
    }

    @Override
    public E getByName(String name) {
        return constructor.construct();
    }

    @Override
    public String getName(E element) {
        return "";
    }

    @Override
    public String getName() {
        return "single:" + id;
    }

    @Override
    public List<E> createElements() {
        return ImmutableList.of(constructor.construct());
    }

    public static interface ILogicProgrammerElementConstructor<E extends ILogicProgrammerElement> {

        public E construct();

    }
}
