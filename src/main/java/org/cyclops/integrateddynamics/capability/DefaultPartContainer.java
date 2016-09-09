package org.cyclops.integrateddynamics.capability;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartContainerFacade;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Default implementation of an {@link IPartContainer}.
 * @author rubensworks
 */
public abstract class DefaultPartContainer implements IPartContainer {

    private final EnumFacingMap<PartHelpers.PartStateHolder<?, ?>> partData = EnumFacingMap.newMap();

    @Override
    public void update() {
        if(!MinecraftHelpers.isClientSide()) {
            // Loop over all part states to check their dirtiness
            for (PartHelpers.PartStateHolder<?, ?> partStateHolder : partData.values()) {
                if (partStateHolder.getState().isDirtyAndReset()) {
                    markDirty();
                }
                if (partStateHolder.getState().isUpdateAndReset()) {
                    sendUpdate();
                }
            }
        }
    }

    @Override
    public DimPos getPosition() {
        return DimPos.of(getWorld(), getPos());
    }

    @Override
    public Map<EnumFacing, IPartType<?, ?>> getParts() {
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
    public <P extends IPartType<P, S>, S extends IPartState<P>> boolean canAddPart(EnumFacing side, IPartType<P, S> part) {
        return !hasPart(side);
    }

    @Override
    public void setPart(final EnumFacing side, final IPartType part, final IPartState partState) {
        PartHelpers.setPart(getNetwork(), getWorld(), getPos(), side, Objects.requireNonNull(part),
                Objects.requireNonNull(partState), new PartHelpers.IPartStateHolderCallback() {
                    @Override
                    public void onSet(PartHelpers.PartStateHolder<?, ?> partStateHolder) {
                        partData.put(side, PartHelpers.PartStateHolder.of(part, partState));
                    }
                });
        onPartsChanged();
    }

    @Override
    public IPartType getPart(EnumFacing side) {
        if(!partData.containsKey(side)) return null;
        return partData.get(side).getPart();
    }

    @Override
    public boolean hasPart(EnumFacing side) {
        return partData.containsKey(side);
    }

    @Override
    public IPartType removePart(EnumFacing side, EntityPlayer player) {
        PartHelpers.PartStateHolder<?, ?> partStateHolder = partData.get(side); // Don't remove the state just yet! We might need it in network removal.
        if(partStateHolder == null) {
            IntegratedDynamics.clog(Level.WARN, "Attempted to remove a part at a side where no part was.");
            return null;
        } else {
            IPartType removed = partStateHolder.getPart();
            if (getNetwork() != null) {
                INetworkElement networkElement = removed
                        .createNetworkElement(getPartContainerFacade(), getPosition(), side);
                networkElement.onPreRemoved(getNetwork());
                if(!getNetwork().removeNetworkElementPre(networkElement)) {
                    return null;
                }

                // Drop all parts types as item.
                List<ItemStack> itemStacks = Lists.newLinkedList();
                networkElement.addDrops(itemStacks, true);
                for(ItemStack itemStack : itemStacks) {
                    if(player != null) {
                        if (!player.capabilities.isCreativeMode) {
                            ItemStackHelpers.spawnItemStackToPlayer(getWorld(), getPos(), itemStack, player);
                        }
                    } else {
                        Block.spawnAsEntity(getWorld(), getPos(), itemStack);
                    }
                }

                // Remove the element from the network.
                getNetwork().removeNetworkElementPost(networkElement);

                networkElement.onPostRemoved(getNetwork());
            } else {
                ItemStackHelpers.spawnItemStackToPlayer(getWorld(), getPos(), new ItemStack(removed.getItem()), player);
            }
            // Finally remove the part data from this tile.
            IPartType ret = partData.remove(side).getPart();
            onPartsChanged();
            return ret;
        }
    }

    @Override
    public void setPartState(EnumFacing side, IPartState partState) {
        PartHelpers.PartStateHolder<?, ?> partStateHolder = partData.get(side);
        if(partStateHolder == null) {
            throw new IllegalArgumentException(String.format("No part at position %s was found to update the state " +
                    "for.", getPosition()));
        }
        partData.put(side, PartHelpers.PartStateHolder.of(partStateHolder.getPart(), partState));
        onPartsChanged();
    }

    @Override
    public IPartState getPartState(EnumFacing side) {
        PartHelpers.PartStateHolder<?, ?> partStateHolder = partData.get(side);
        if(partStateHolder == null) {
            throw new IllegalArgumentException(String.format("No part at position %s was found to get the state from.",
                    getPosition()));
        }
        return partStateHolder.getState();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if(facing == null) {
            for (Map.Entry<EnumFacing, PartHelpers.PartStateHolder<?, ?>> entry : partData.entrySet()) {
                IPartState partState = entry.getValue().getState();
                if(partState != null && partState.hasCapability(capability)) {
                    return true;
                }
            }
        } else {
            if(hasPart(facing)) {
                IPartState partState = getPartState(facing);
                if (partState != null && partState.hasCapability(capability)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if(facing == null) {
            for (Map.Entry<EnumFacing, PartHelpers.PartStateHolder<?, ?>> entry : partData.entrySet()) {
                IPartState partState = entry.getValue().getState();
                if(partState != null && partState.hasCapability(capability)) {
                    return (T) partState.getCapability(capability);
                }
            }
        } else {
            if(hasPart(facing)) {
                IPartState partState = getPartState(facing);
                if (partState != null && partState.hasCapability(capability)) {
                    return (T) partState.getCapability(capability);
                }
            }
        }
        return null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        PartHelpers.writePartsToNBT(getPos(), tag, this.partData);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        PartHelpers.readPartsFromNBT(getNetwork(), getPos(), tag, this.partData, getWorld());
    }

    protected void onPartsChanged() {
        markDirty();
        sendUpdate();
    }

    protected abstract void markDirty();
    protected abstract void sendUpdate();
    protected abstract World getWorld();
    protected abstract BlockPos getPos();
    protected abstract IPartNetwork getNetwork();
    protected abstract IPartContainerFacade getPartContainerFacade();

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
    public void setPartData(Map<EnumFacing, PartHelpers.PartStateHolder<?, ?>> partData) {
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
