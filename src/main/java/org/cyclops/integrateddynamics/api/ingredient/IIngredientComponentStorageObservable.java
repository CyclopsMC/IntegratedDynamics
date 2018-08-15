package org.cyclops.integrateddynamics.api.ingredient;

import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.ingredient.collection.IIngredientCollection;
import org.cyclops.cyclopscore.ingredient.collection.diff.IngredientCollectionDiff;
import org.cyclops.integrateddynamics.api.part.PartPos;

/**
 * An observable ingredient component storage.
 * @param <T> The instance type.
 * @param <M> The matching condition parameter, may be Void. Instances MUST properly implement the equals method.
 * @author rubensworks
 */
public interface IIngredientComponentStorageObservable<T, M> {

    /**
     * @return The ingredient component type this storage applies to.
     */
    public IngredientComponent<T, M> getComponent();

    /**
     * Add an observer for listing to index change events.
     * @param observer An index change observer.
     */
    public void addObserver(IIndexChangeObserver<T, M> observer);

    /**
     * Remove the given index change observer.
     * This will silently fail if the given observer was not registered.
     * @param observer An index change observer.
     */
    public void removeObserver(IIndexChangeObserver<T, M> observer);

    /**
     * An observer for listening to storage changes.
     * @param <T> The instance type.
     * @param <M> The match condition type.
     */
    public static interface IIndexChangeObserver<T, M> {
        /**
         * Called when a change event is emitted.
         * @param event A storage change event.
         */
        public void onChange(StorageChangeEvent<T, M> event);
    }

    /**
     * A storage change event.
     * This is thrown for either additions or deletions,
     * as identified by the change type.
     * @param <T> The instance type.
     * @param <M> The match condition type.
     */
    public static class StorageChangeEvent<T, M> {
        private final int channel;
        private final PartPos pos;
        private final Change changeType;
        private final boolean completeChange;
        private final IIngredientCollection<T, M> instances;

        public StorageChangeEvent(int channel, PartPos pos,
                                  Change changeType, boolean completeChange, IIngredientCollection<T, M> instances) {
            this.channel = channel;
            this.pos = pos;
            this.changeType = changeType;
            this.completeChange = completeChange;
            this.instances = instances;
        }

        public int getChannel() {
            return channel;
        }

        public PartPos getPos() {
            return pos;
        }

        /**
         * @return The type of change.
         */
        public Change getChangeType() {
            return changeType;
        }

        /**
         * @return If the change is complete.
         *         In the case of additions, this means that the storage became completely full.
         *         In the case of deletions, this means that the storage became completely empty.
         */
        public boolean isCompleteChange() {
            return completeChange;
        }

        /**
         * @return The instances that were added or removed.
         */
        public IIngredientCollection<T, M> getInstances() {
            return instances;
        }

        @Override
        public String toString() {
            return String.format("[%s at %s(%s): %s]", getChangeType().name(), getPos(), getChannel(), getInstances());
        }

        /**
         * @return Get the collection diff from this event.
         */
        public IngredientCollectionDiff<T, M> getDiff() {
            return new IngredientCollectionDiff<>(
                    changeType == IIngredientComponentStorageObservable.Change.ADDITION ? getInstances() : null,
                    changeType == IIngredientComponentStorageObservable.Change.DELETION ? getInstances() : null,
                    false);
        }
    }

    /**
     * A type of change.
     */
    public static enum Change {
        ADDITION,
        DELETION
    }

}
