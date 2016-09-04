package org.cyclops.integrateddynamics.modcompat.refinedstorage.aspect;

import java.util.AbstractList;
import java.util.List;

/**
 * A lazy composite list of lists.
 * @author rubensworks
 */
public class LazyCompositeList<V> extends AbstractList<V> {

    private final List<List<V>> inputs;

    public LazyCompositeList(List<List<V>> inputs) {
        this.inputs = inputs;
    }

    @Override
    public V get(int index) {
        int itIndex = index;
        for (List<V> input : inputs) {
            int size = input.size();
            if (itIndex < size) {
                return input.get(itIndex);
            } else {
                itIndex -= size;
            }
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int size() {
        int size = 0;
        for (List<V> input : inputs) {
            size += input.size();
        }
        return size;
    }
}
