package org.cyclops.integrateddynamics.core.network;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.datastructure.CompositeMap;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.block.IEnergyBattery;
import org.cyclops.integrateddynamics.api.block.IEnergyBatteryFacade;
import org.cyclops.integrateddynamics.api.block.IVariableContainerFacade;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.network.*;
import org.cyclops.integrateddynamics.api.part.*;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.read.IPartStateReader;
import org.cyclops.integrateddynamics.api.part.read.IPartTypeReader;
import org.cyclops.integrateddynamics.api.path.ICablePathElement;
import org.cyclops.integrateddynamics.core.path.Cluster;
import org.cyclops.integrateddynamics.core.path.PathFinder;
import org.cyclops.integrateddynamics.core.persist.world.NetworkWorldStorage;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A network that can hold parts.
 * Note that this network only contains references to the relevant data, it does not contain the actual information.
 * @author rubensworks
 */
public class PartNetwork extends Network<IPartNetwork> implements IPartNetwork, IEnergyNetwork {

    private Map<Integer, PartPos> partPositions;
    private List<DimPos> variableContainerPositions;
    private Map<Integer, IVariableFacade> compositeVariableCache;
    private Map<Integer, IValue> lazyExpressionValueCache;
    private Map<DimPos, IEnergyBatteryFacade> energyBatteryPositions;

    private volatile boolean partsChanged = false;

    /**
     * This constructor should not be called, except for the process of constructing networks from NBT.
     */
    public PartNetwork() {
        super();
    }

    /**
     * Create a new network from a given cluster of cables.
     * Each cable will be checked if it is an instance of {@link INetworkElementProvider} and will add all its
     * elements to the network in that case.
     * Each cable that is an instance of {@link IPartContainerFacade}
     * will have the network stored in its part container.
     * @param cables The cables that make up the connections in the network which can potentially provide network
     *               elements.
     */
    public PartNetwork(Cluster<ICablePathElement> cables) {
        super(cables);
    }

    @Override
    protected void onConstruct() {
        super.onConstruct();
        partPositions = Maps.newHashMap();
        variableContainerPositions = Lists.newLinkedList();
        compositeVariableCache = null;
        lazyExpressionValueCache = Maps.newHashMap();
        energyBatteryPositions = Maps.newHashMap();
    }

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
        return TileMultipartTicking.get(partPos.getPos()).getPartState(partPos.getSide());
    }

    @Override
    public IPartType getPartType(int partId) {
        PartPos partPos = partPositions.get(partId);
        return TileMultipartTicking.get(partPos.getPos()).getPart(partPos.getSide());
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
        IPartContainer partContainer = TileMultipartTicking.get(partPos.getPos());
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
        return ((IPartTypeReader) getPartType(partId)).getVariable(
                PartTarget.fromCenter(partPositions.get(partId)), (IPartStateReader) partState, aspect) != null;
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
                World world = dimPos.getWorld();
                BlockPos pos = dimPos.getBlockPos();
                Block block = world.getBlockState(pos).getBlock();
                if(block instanceof IVariableContainerFacade) {
                    compositeMap.addElement(((IVariableContainerFacade) block).getVariableContainer(world, pos).getVariableCache());
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
    public boolean equals(Object object) {
        return object instanceof PartNetwork && areNetworksEqual(this, (PartNetwork) object);
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
    public void notifyPartsChanged() {
        this.partsChanged = true;
    }

    private void onPartsChanged() {
        System.out.println("Parts of network " + this + " are changed.");
    }

    @Override
    protected boolean canUpdate(INetworkElement<IPartNetwork> element) {
        if(!super.canUpdate(element)) return false;
        if(element instanceof IEnergyConsumingNetworkElement) return true;
        int multiplier = GeneralConfig.energyConsumptionMultiplier;
        if(multiplier == 0) return true;
        int consumptionRate = ((IEnergyConsumingNetworkElement) element).getConsumptionRate() * multiplier;
        return consume(consumptionRate, true) == consumptionRate;
    }

    @Override
    protected void postUpdate(INetworkElement<IPartNetwork> element) {
        super.postUpdate(element);
        if(element instanceof IEnergyConsumingNetworkElement) {
            int multiplier = GeneralConfig.energyConsumptionMultiplier;
            if (multiplier > 0) {
                int consumptionRate = ((IEnergyConsumingNetworkElement) element).getConsumptionRate() * multiplier;
                consume(consumptionRate, false);
            }
        }
    }

    @Override
    protected void onUpdate() {
        super.onUpdate();
        // Reset lazy variable cache
        lazyExpressionValueCache.clear();

        // Signal parts of any changes
        if (partsChanged) {
            this.partsChanged = false;
            onPartsChanged();
        }
    }

    @Override
    public boolean removeCable(Block block, ICablePathElement cable) {
        if(super.removeCable(block, cable)) {
            notifyPartsChanged();
            return true;
        }
        return false;
    }

    /**
     * Initiate a full network from the given start position.
     * @param connectable The cable to start the network from.
     * @param world The world.
     * @param pos The position.
     * @return The newly formed network.
     */
    public static PartNetwork initiateNetworkSetup(ICable<ICablePathElement> connectable, World world, BlockPos pos) {
        PartNetwork network = new PartNetwork(PathFinder.getConnectedCluster(connectable.createPathElement(world, pos)));
        NetworkWorldStorage.getInstance(IntegratedDynamics._instance).addNewNetwork(network);
        return network;
    }

    protected synchronized List<IEnergyBattery> getMaterializedEnergyBatteries() {
        return ImmutableList.copyOf(Iterables.transform(energyBatteryPositions.entrySet(), new Function<Map.Entry<DimPos, IEnergyBatteryFacade>, IEnergyBattery>() {
            @Nullable
            @Override
            public IEnergyBattery apply(Map.Entry<DimPos, IEnergyBatteryFacade> input) {
                return input.getValue().getEnergyBattery(input.getKey().getWorld(), input.getKey().getBlockPos());
            }

            @Override
            public boolean equals(@Nullable Object object) {
                return false;
            }
        }));
    }

    protected int addSafe(int a, int b) {
        int add = a + b;
        if(add < a || add < b) return Integer.MAX_VALUE;
        return add;
    }

    @Override
    public synchronized int getStoredEnergy() {
        int energy = 0;
        for(IEnergyBattery energyBattery : getMaterializedEnergyBatteries()) {
            energy = addSafe(energy, energyBattery.getStoredEnergy());
        }
        return energy;
    }

    @Override
    public synchronized int getMaxStoredEnergy() {
        int maxEnergy = 0;
        for(IEnergyBattery energyBattery : getMaterializedEnergyBatteries()) {
            maxEnergy = addSafe(maxEnergy, energyBattery.getMaxStoredEnergy());
        }
        return maxEnergy;
    }

    @Override
    public int addEnergy(int energy, boolean simulate) {
        int toAdd = energy;
        for(IEnergyBattery energyBattery : getMaterializedEnergyBatteries()) {
            int maxAdd = Math.min(energyBattery.getMaxStoredEnergy() - energyBattery.getStoredEnergy(), toAdd);
            if(maxAdd > 0 && !simulate) {
                energyBattery.addEnergy(maxAdd);
            }
            toAdd -= maxAdd;
        }
        return energy - toAdd;
    }

    @Override
    public synchronized int consume(int energy, boolean simulate) {
        int toConsume = energy;
        for(IEnergyBattery energyBattery : getMaterializedEnergyBatteries()) {
            int consume = Math.min(energyBattery.getStoredEnergy(), toConsume);
            if(consume > 0) {
                toConsume -= energyBattery.consume(consume, simulate);
            }
        }
        return energy - toConsume;
    }

    @Override
    public boolean addEnergyBattery(DimPos dimPos) {
        World world = dimPos.getWorld();
        BlockPos pos = dimPos.getBlockPos();
        Block block = world.getBlockState(pos).getBlock();
        if(block instanceof IEnergyBatteryFacade) {
            return energyBatteryPositions.put(dimPos, (IEnergyBatteryFacade) block) == null;
        }
        return false;
    }

    @Override
    public void removeEnergyBattery(DimPos pos) {
        energyBatteryPositions.remove(pos);
    }

    @Override
    public Map<DimPos, IEnergyBatteryFacade> getEnergyBatteries() {
        return Collections.unmodifiableMap(energyBatteryPositions);
    }

    @Override
    public int getConsumptionRate() {
        int multiplier = GeneralConfig.energyConsumptionMultiplier;
        if(multiplier == 0) return 0;
        int consumption = 0;
        for(INetworkElement element : getElements()) {
            consumption += ((IEnergyConsumingNetworkElement) element).getConsumptionRate() * multiplier;
        }
        return consumption;
    }
}
