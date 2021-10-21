package org.cyclops.integrateddynamics.core.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.cyclops.commoncapabilities.api.capability.inventorystate.IInventoryState;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetworkIngredients;
import org.cyclops.integrateddynamics.api.part.PartPos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A thread-safe proxy for reading ingredients within a world.
 * @author rubensworks
 */
public class ConcurrentWorldIngredientsProxy<T, M> {

    private final IPositionedAddonsNetworkIngredients<T, M> network;
    private Set<PartPos> oldPositions;
    private final Map<PartPos, Integer> states;
    private final Map<PartPos, Collection<T>> instances;
    private final Set<PartPos> readStates;
    private final Set<PartPos> readInstances;

    public ConcurrentWorldIngredientsProxy(IPositionedAddonsNetworkIngredients<T, M> network) {
        this.network = network;
        this.oldPositions = Sets.newHashSet();
        this.states = new ConcurrentHashMap<>();
        this.instances = new ConcurrentHashMap<>();
        this.readStates = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.readInstances = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    public IPositionedAddonsNetworkIngredients<T, M> getNetwork() {
        return network;
    }

    public Optional<Integer> getInventoryState(PartPos pos) {
        Optional<Integer> value = Optional.ofNullable(this.states.get(pos));
        this.readStates.add(pos);
        return value;
    }

    public Collection<T> getInstances(PartPos pos) {
        Collection<T> value = instances.getOrDefault(pos, Collections.emptyList());
        this.readInstances.add(pos);
        return value;
    }

    protected Collection<PartPos> getPositions() {
        return Lists.newArrayList(getNetwork().getPositions());
    }

    public void onWorldTick() {
        Collection<PartPos> newPositions = getPositions();

        // Do nothing if nothing was read, and no positions were changed
        boolean positionsChanged = newPositions.size() != this.oldPositions.size() || !newPositions.containsAll(this.oldPositions);
        if (this.readStates.isEmpty() && this.readInstances.isEmpty() && !positionsChanged) {
            return;
        }

        // Update states and ingredients for all positions
        for (PartPos pos : newPositions) {
            if (positionsChanged) {
                this.oldPositions.remove(pos);
            }

            // Fetch inventory states
            if (this.readStates.contains(pos) || !this.states.containsKey(pos)) {
                IInventoryState inventoryState = TileHelpers
                        .getCapability(pos.getPos(), pos.getSide(), Capabilities.INVENTORY_STATE)
                        .orElse(null);
                if (inventoryState != null) {
                    int newState = inventoryState.getState();
                    Integer previousState = this.states.put(pos, newState);

                    // If we find a state change, make sure that we also reload the instances in this iteration
                    if (previousState == null || newState != previousState) {
                        this.readInstances.add(pos);
                    }
                } else {
                    this.states.remove(pos);
                }
                this.readStates.remove(pos);
            }

            // Fetch ingredient instances
            if (this.readInstances.contains(pos) || !this.instances.containsKey(pos)) {
                ArrayList<T> instances = Lists.newArrayList(getNetwork().getRawInstances(pos));
                this.instances.put(pos, instances);
                this.readInstances.remove(pos);
            }
        }

        // Remove positions that are not in the network anymore
        if (positionsChanged) {
            for (PartPos oldPosition : this.oldPositions) {
                this.readStates.remove(oldPosition);
                this.readInstances.remove(oldPosition);
                this.states.remove(oldPosition);
                this.instances.remove(oldPosition);
            }
            this.oldPositions = Sets.newHashSet(newPositions);
        }
    }

}
