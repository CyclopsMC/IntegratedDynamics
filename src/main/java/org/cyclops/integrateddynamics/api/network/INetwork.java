package org.cyclops.integrateddynamics.api.network;

import net.minecraft.block.Block;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.persist.nbt.INBTSerializable;
import org.cyclops.integrateddynamics.api.evaluate.expression.ILazyExpressionValueCache;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.network.event.INetworkEventBus;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.core.path.CablePathElement;

/**
 * A network can hold a set of {@link INetworkElement}s.
 * Note that this network only contains references to the relevant data, it does not contain the actual information.
 * @author rubensworks
 */
public interface INetwork extends INBTSerializable, ILazyExpressionValueCache {

    /**
     * @return The event bus for this network.
     */
    public INetworkEventBus getEventBus();

    /**
     * Add the given part state to the network.
     * @param partId The id of the part.
     * @param partPos The part position to add.
     * @return If the addition was successful.
     */
    public boolean addPart(int partId, PartPos partPos);

    /**
     * Get the part state by id from this network.
     * @param partId The part state id.
     * @return The corresponding part state or null.
     */
    public IPartState getPartState(int partId);

    /**
     * Get the part by id from this network.
     * @param partId The part state id.
     * @return The corresponding part or null.
     */
    public IPartType getPartType(int partId);

    /**
     * Remove the part state by id from this network.
     * @param partId The part state id.
     */
    public void removePart(int partId);

    /**
     * Check if this network contains the given part id.
     * @param partId The part state id.
     * @return If this part is present in this network.
     */
    public boolean hasPart(int partId);

    /**
     * Check if a variable can be found for a given part and aspect.
     * @param partId The part state id.
     * @param aspect The aspect from the given part.
     * @param <V> The value.
     * @return True if such a variable can be found. False if the given part is not present in the network or if the
     *         given aspect is not present at that part.
     */
    public <V extends IValue> boolean hasPartVariable(int partId, IAspectRead<V, ?> aspect);

    /**
     * Get the current variable from the aspect of the given part id.
     * This method can call a NPE or cast exception when the given part does not exists, so make sure to check this before.
     * @param partId The part state id.
     * @param aspect The aspect from the given part.
     * @param <V> The value.
     * @return The variable.
     */
    public <V extends IValue> IVariable<V> getPartVariable(int partId, IAspectRead<V, ?> aspect);

    /**
     * Check if this network has access to the variable facade with given variable id.
     * @param variableId The variable id.
     * @return If this network has access to it.
     */
    public boolean hasVariableFacade(int variableId);

    /**
     * Get the variable facade with given variable id.
     * @param variableId The variable id.
     * @return The variable facade.
     */
    public IVariableFacade getVariableFacade(int variableId);

    /**
     * Add the position of a variable container.
     * @param dimPos The variable container position.
     * @return If the container did not exist in the network already.
     */
    public boolean addVariableContainer(DimPos dimPos);

    /**
     * Remove the position of a variable container.
     * @param dimPos The variable container position.
     */
    public void removeVariableContainer(DimPos dimPos);

    /**
     * Add a given network element to the network
     * Also checks if it can tick and will handle it accordingly.
     * @param element The network element.
     * @param networkPreinit If the network is still in the process of being initialized.
     * @return If the addition succeeded.
     */
    public boolean addNetworkElement(INetworkElement element, boolean networkPreinit);

    /**
     * Add a given network element to the tickable elements set.
     * @param element The network element.
     */
    public void addNetworkElementUpdateable(INetworkElement element);

    /**
     * Remove a given network element from the network.
     * Also removed its tickable instance.
     * @param element The network element.
     */
    public void removeNetworkElement(INetworkElement element);

    /**
     * Remove given network element from the tickable elements set.
     * @param element The network element.
     */
    public void removeNetworkElementUpdateable(INetworkElement element);

    /**
     * Terminate the network elements for this network.
     */
    public void kill();

    /**
     * Kills the network is it had no more network elements.
     * @return If the network was killed.
     */
    public boolean killIfEmpty();

    /**
     * This network updating should be called each tick.
     */
    public void update();

    /**
     * Tell the network to recheck all parts next update round.
     */
    public void notifyPartsChanged();

    /**
     * Remove the given cable from the network.
     * If the cable had any network elements registered in the network, these will be killed and removed as well.
     * @param block The block instance of the cable element.
     * @param cable The actual cable instance.
     */
    public void removeCable(Block block, CablePathElement cable);

    /**
     * Called when the server loaded this network.
     * This is the time to notify all network elements of this network.
     */
    public void afterServerLoad();

    /**
     * Called when the server will save this network before stopping.
     * This is the time to notify all network elements of this network.
     */
    public void beforeServerStop();

}
