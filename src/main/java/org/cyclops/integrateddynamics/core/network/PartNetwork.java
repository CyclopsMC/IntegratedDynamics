package org.cyclops.integrateddynamics.core.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.datastructure.CompositeMap;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.block.IVariableContainer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.network.FullNetworkListenerAdapter;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.*;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.read.IPartStateReader;
import org.cyclops.integrateddynamics.api.part.read.IPartTypeReader;
import org.cyclops.integrateddynamics.api.path.IPathElement;
import org.cyclops.integrateddynamics.capability.variablecontainer.VariableContainerConfig;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A network that can hold parts.
 * Note that this network only contains references to the relevant data, it does not contain the actual information.
 * @author rubensworks
 */
public class PartNetwork extends FullNetworkListenerAdapter implements IPartNetwork {

    @Getter
    @Setter
    private INetwork network;
    private Map<Integer, PartPos> partPositions = Maps.newHashMap();
    private List<DimPos> variableContainerPositions = Lists.newArrayList();
    private Map<Integer, IVariableFacade> compositeVariableCache = null;
    private Map<Integer, IValue> lazyExpressionValueCache = Maps.newHashMap();
    private Map<Integer, DimPos> proxyPositions = Maps.newHashMap();

    private volatile boolean partsChanged = false;

    @Override
    public boolean addPart(int partId, PartPos partPos) {
        if(partPositions.containsKey(partId)) {
            return false;
        }
        partPositions.put(partId, partPos);
        return true;
    }

    @Override
    public IPartState getPartState(int partId) {
        PartPos partPos = partPositions.get(partId);
        return PartHelpers.getPartContainer(partPos.getPos()).getPartState(partPos.getSide());
    }

    @Override
    public IPartType getPartType(int partId) {
        PartPos partPos = partPositions.get(partId);
        return PartHelpers.getPartContainer(partPos.getPos()).getPart(partPos.getSide());
    }

    @Override
    public void removePart(int partId) {
        partPositions.remove(partId);
    }

    @Override
    public boolean hasPart(int partId) {
        if(!partPositions.containsKey(partId)) {
            return false;
        }
        PartPos partPos = partPositions.get(partId);
        IPartContainer partContainer = PartHelpers.getPartContainer(partPos.getPos());
        return partContainer != null && partContainer.hasPart(partPos.getSide());
    }

    @Override
    public <V extends IValue> boolean hasPartVariable(int partId, IAspectRead<V, ?> aspect) {
        if(!hasPart(partId)) {
            return false;
        }
        IPartState partState = getPartState(partId);
        if(!(partState instanceof IPartStateReader)) {
            return false;
        }
        IPartType partType = getPartType(partId);
        if(!(partType instanceof IPartTypeReader)) {
            return false;
        }
        try {
            return ((IPartTypeReader) getPartType(partId)).getVariable(
                    PartTarget.fromCenter(partPositions.get(partId)), (IPartStateReader) partState, aspect) != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public <V extends IValue> IVariable<V> getPartVariable(int partId, IAspectRead<V, ?> aspect) {
        return ((IPartStateReader) getPartState(partId)).getVariable(aspect);
    }

    protected Map<Integer, IVariableFacade> getVariableCache() {
        if(compositeVariableCache == null) {
            // Create a new composite map view on the existing variable containers in this network.
            CompositeMap<Integer, IVariableFacade> compositeMap = new CompositeMap<>();
            for(Iterator<DimPos> it = variableContainerPositions.iterator(); it.hasNext();) {
                DimPos dimPos = it.next();
                IVariableContainer variableContainer = TileHelpers.getCapability(dimPos, null, VariableContainerConfig.CAPABILITY);
                if(variableContainer != null) {
                    compositeMap.addElement(variableContainer.getVariableCache());
                } else {
                    IntegratedDynamics.clog(Level.ERROR, "The variable container at " + dimPos + " was invalid, skipping.");
                    it.remove();
                }
            }
            compositeVariableCache = compositeMap;
        }
        return compositeVariableCache;
    }

    @Override
    public boolean hasVariableFacade(int variableId) {
        return getVariableCache().containsKey(variableId);
    }

    @Override
    public IVariableFacade getVariableFacade(int variableId) {
        return getVariableCache().get(variableId);
    }

    @Override
    public void setValue(int id, IValue value) {
        lazyExpressionValueCache.put(id, value);
    }

    @Override
    public boolean hasValue(int id) {
        return lazyExpressionValueCache.containsKey(id);
    }

    @Override
    public IValue getValue(int id) {
        return lazyExpressionValueCache.get(id);
    }

    @Override
    public boolean addVariableContainer(DimPos dimPos) {
        compositeVariableCache = null;
        return variableContainerPositions.add(dimPos);
    }

    @Override
    public void removeVariableContainer(DimPos dimPos) {
        compositeVariableCache = null;
        variableContainerPositions.remove(dimPos);
    }

    @Override
    public boolean addProxy(int proxyId, DimPos dimPos) {
        if(proxyPositions.containsKey(proxyId)) {
            return false;
        }
        proxyPositions.put(proxyId, dimPos);
        return true;
    }

    @Override
    public void removeProxy(int proxyId) {
        proxyPositions.remove(proxyId);
    }

    @Override
    public DimPos getProxy(int proxyId) {
        return proxyPositions.get(proxyId);
    }

    @Override
    public void notifyPartsChanged() {
        this.partsChanged = true;
    }

    private void onPartsChanged() {

    }

    @Override
    public void update() {
        // Reset lazy variable cache
        lazyExpressionValueCache.clear();

        // Signal parts of any changes
        if (partsChanged) {
            this.partsChanged = false;
            onPartsChanged();
        }
    }

    @Override
    public boolean removePathElement(IPathElement pathElement) {
        notifyPartsChanged();
        return true;
    }
}
