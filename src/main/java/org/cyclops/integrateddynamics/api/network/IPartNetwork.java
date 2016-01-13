package org.cyclops.integrateddynamics.api.network;

import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.evaluate.expression.ILazyExpressionValueCache;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;

/**
 * A network that can hold parts.
 * Note that this network only contains references to the relevant data, it does not contain the actual information.
 * @author rubensworks
 */
public interface IPartNetwork extends INetwork<IPartNetwork>, ILazyExpressionValueCache {

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
     * Add the given proxy to the network.
     * @param proxyId The id of the proxy.
     * @param dimPos The proxy position.
     * @return If the addition was successful.
     */
    public boolean addProxy(int proxyId, DimPos dimPos);

    /**
     * Remove the proxy by id from this network.
     * @param proxyId The id of the proxy.
     */
    public void removeProxy(int proxyId);

    /**
     * Check if this network contains the given part id.
     * @param proxyId The id of the proxy.
     * @return The proxy position.
     */
    public DimPos getProxy(int proxyId);

    /**
     * Tell the network to recheck all parts next update round.
     */
    public void notifyPartsChanged();

}
