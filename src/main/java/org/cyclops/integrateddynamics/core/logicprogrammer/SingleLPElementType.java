package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElement;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementType;

import java.util.List;

/**
 * Element type that provides exactly one element.
 * @author rubensworks
 */
public class SingleLPElementType<E extends ILogicProgrammerElement> implements ILogicProgrammerElementType<E> {

    private final ILogicProgrammerElementConstructor<E> constructor;
    private final String id;

    public SingleLPElementType(ILogicProgrammerElementConstructor<E> constructor, String id) {
        this.constructor = constructor;
        this.id = id;
    }

    @Override
    public E getByName(ResourceLocation name) {
        return constructor.construct();
    }

    @Override
    public ResourceLocation getName(E element) {
        return new ResourceLocation("");
    }

    @Override
    public ResourceLocation getUniqueName() {
        return new ResourceLocation(Reference.MOD_ID, "single_" + id);
    }

    @Override
    public List<E> createElements() {
        return ImmutableList.of(constructor.construct());
    }

    public static interface ILogicProgrammerElementConstructor<E extends ILogicProgrammerElement> {

        public E construct();

    }
}
