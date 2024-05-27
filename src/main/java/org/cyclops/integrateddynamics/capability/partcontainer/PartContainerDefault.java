package org.cyclops.integrateddynamics.capability.partcontainer;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.PartStateException;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartCapability;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Default implementation of an {@link IPartContainer}.
 * @author rubensworks
 */
public abstract class PartContainerDefault implements IPartContainer {

    protected final EnumFacingMap<PartHelpers.PartStateHolder<?, ?>> partData = EnumFacingMap.newMap();

    @Override
    public void update() {
        if(!MinecraftHelpers.isClientSideThread()) {
            // Loop over all part states to check their dirtiness
            for (PartHelpers.PartStateHolder<?, ?> partStateHolder : partData.values()) {
                if (partStateHolder.getState().isDirtyAndReset()) {
                    setChanged();
                }
                if (partStateHolder.getState().isUpdateAndReset()) {
                    sendUpdate();
                }
            }
        }
    }

    @Override
    public DimPos getPosition() {
        return DimPos.of(getLevel(), getPos());
    }

    @Override
    public Map<Direction, IPartType<?, ?>> getParts() {
        return Maps.transformValues(partData, new Function<PartHelpers.PartStateHolder<?, ?>, IPartType<?, ?>>() {
            @Nullable
            @Override
            public IPartType<?, ?> apply(@Nullable PartHelpers.PartStateHolder<?, ?> input) {
                return input.getPart();
            }
        });
    }

    @Override
    public boolean hasParts() {
        return !partData.isEmpty();
    }

    @Override
    public <P extends IPartType<P, S>, S extends IPartState<P>> boolean canAddPart(Direction side, IPartType<P, S> part) {
        return !hasPart(side);
    }

    @Override
    public <P extends IPartType<P, S>, S extends IPartState<P>>void setPart(final Direction side, final IPartType<P, S> part, final IPartState<P> partState) {
        PartHelpers.setPart(getNetwork(), getLevel(), getPos(), side, Objects.requireNonNull(part),
                Objects.requireNonNull(partState), new PartHelpers.IPartStateHolderCallback() {
                    @Override
                    public void onSet(PartHelpers.PartStateHolder<?, ?> partStateHolder) {
                        partData.put(side, PartHelpers.PartStateHolder.of(part, partState));
                        sendUpdate();
                    }
                });
        onPartsChanged();
    }

    @Override
    public IPartType getPart(Direction side) {
        PartHelpers.PartStateHolder<?, ?> partStateHolder = partData.get(side);
        return partStateHolder != null ? partStateHolder.getPart() : null;
    }

    @Override
    public boolean hasPart(Direction side) {
        return partData.containsKey(side);
    }

    @Override
    public IPartType removePart(Direction side, Player player, boolean dropMainElement, boolean saveState) {
        PartHelpers.PartStateHolder<?, ?> partStateHolder = partData.get(side); // Don't remove the state just yet! We might need it in network removal.
        if(partStateHolder == null) {
            IntegratedDynamics.clog(org.apache.logging.log4j.Level.WARN, "Attempted to remove a part at a side where no part was.");
            return null;
        } else {
            IPartType removed = partStateHolder.getPart();
            if (getNetwork() != null) {
                INetworkElement networkElement = removed.createNetworkElement(this, getPosition(), side);
                networkElement.onPreRemoved(getNetwork());
                if(!getNetwork().removeNetworkElementPre(networkElement)) {
                    return null;
                }

                // Don't drop main element when in creative mode
                if(player != null && player.isCreative()) {
                    dropMainElement = false;
                }

                // Drop all parts types as item.
                List<ItemStack> itemStacks = Lists.newLinkedList();
                networkElement.addDrops(itemStacks, dropMainElement, saveState);
                for(ItemStack itemStack : itemStacks) {
                    if(player != null) {
                        ItemStackHelpers.spawnItemStackToPlayer(getLevel(), getPos(), itemStack, player);
                    } else {
                        ItemStackHelpers.spawnItemStack(getLevel(), getPos(), itemStack);
                    }
                }

                // Remove the element from the network.
                getNetwork().removeNetworkElementPost(networkElement);

                // Finally remove the part data from this part.
                IPartType ret = partData.remove(side).getPart();

                networkElement.onPostRemoved(getNetwork());

                onPartsChanged();
                return ret;
            } else if (dropMainElement) {
                ItemStack itemStack = removed.getItemStack(partStateHolder.getState(), saveState);
                if(player != null) {
                    if (!player.isCreative()) {
                        ItemStackHelpers.spawnItemStackToPlayer(getLevel(), getPos(), itemStack, player);
                    }
                } else {
                    ItemStackHelpers.spawnItemStack(getLevel(), getPos(), itemStack);
                }
            }
            // Finally remove the part data from this part.
            IPartType ret = partData.remove(side).getPart();
            onPartsChanged();
            return ret;
        }
    }

    @Override
    public void setPartState(Direction side, IPartState partState) throws PartStateException {
        if(!hasPart(side)) {
            throw new PartStateException(getPosition(), side);
        }
        partData.put(side, PartHelpers.PartStateHolder.of(getPart(side), partState));
        onPartsChanged();
    }

    @Override
    public IPartState getPartState(Direction side) throws PartStateException {
        synchronized (partData) {
            PartHelpers.PartStateHolder<?, ?> partStateHolder = partData.get(side);
            if (partStateHolder == null) {
                throw new PartStateException(getPosition(), side);
            }
            return partStateHolder.getState();
        }
    }

    @Override
    public <T> Optional<T> getCapability(PartCapability<T> partCapability, INetwork network, IPartNetwork partNetwork, PartTarget target) {
        if (hasPart(target.getCenter().getSide())) {
            IPartState partState = getPartState(target.getCenter().getSide());
            if (partState != null) {
                partState.getCapability(
                        getPart(target.getCenter().getSide()),
                        partCapability,
                        network,
                        partNetwork,
                        target
                );
            }
        }
        return Optional.empty();
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        PartHelpers.writePartsToNBT(getPos(), tag, this.partData);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        synchronized (this.partData) {
            PartHelpers.readPartsFromNBT(getNetwork(), getPos(), tag, this.partData, getLevel());
        }
    }

    protected void onPartsChanged() {
        setChanged();
        sendUpdate();
    }

    protected abstract void setChanged();
    protected abstract void sendUpdate();
    protected abstract Level getLevel();
    protected abstract BlockPos getPos();
    protected abstract INetwork getNetwork();

    protected Optional<IPartNetwork> getPartNetwork() {
        return NetworkHelpers.getPartNetwork(getNetwork());
    }

    /**
     * @return The raw part data.
     */
    public EnumFacingMap<PartHelpers.PartStateHolder<?, ?>> getPartData() {
        return this.partData;
    }

    /**
     * Override the part data.
     * @param partData The raw part data.
     */
    public void setPartData(Map<Direction, PartHelpers.PartStateHolder<?, ?>> partData) {
        this.partData.clear();
        this.partData.putAll(partData);
    }

    /**
     * Reset the part data without signaling any neighbours or the network.
     * Is used in block conversion.
     */
    public void silentResetPartData() {
        this.partData.clear();
    }

}
