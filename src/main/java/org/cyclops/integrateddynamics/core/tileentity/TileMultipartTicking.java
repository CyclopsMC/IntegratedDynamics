package org.cyclops.integrateddynamics.core.tileentity;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import lombok.Data;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.tileentity.TickingCyclopsTileEntity;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.parts.EnumPartType;
import org.cyclops.integrateddynamics.core.parts.IPart;
import org.cyclops.integrateddynamics.core.parts.IPartContainer;
import org.cyclops.integrateddynamics.core.parts.IPartState;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * A ticking tile entity which is made up of different parts.
 * @author Ruben Taelman
 */
public class TileMultipartTicking extends TickingCyclopsTileEntity implements IPartContainer {

    private final Map<EnumFacing, PartStateHolder<?, ?>> partData = Maps.newHashMap();

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        NBTTagList partList = new NBTTagList();
        for(Map.Entry<EnumFacing, PartStateHolder<?, ?>> entry : partData.entrySet()) {
            NBTTagCompound partTag = new NBTTagCompound();
            IPart part = entry.getValue().getPart();
            IPartState partState = entry.getValue().getState();
            partTag.setString("__partType", part.getType().getName());
            partTag.setString("__side", entry.getKey().getName());
            part.toNBT(partTag, partState);
            partList.appendTag(partTag);
        }
        tag.setTag("parts", partList);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        NBTTagList partList = tag.getTagList("parts", MinecraftHelpers.NBTTag_Types.NBTTagList.ordinal());
        for(int i = 0; i < partList.tagCount(); i++) {
            NBTTagCompound partTag = partList.getCompoundTagAt(i);
            EnumPartType type = EnumPartType.getInstance(partTag.getString("__partType"));
            if(type != null) {
                EnumFacing side = EnumFacing.byName(partTag.getString("__side"));
                if(side != null) {
                    IPart part = type.getPart();
                    IPartState partState = part.fromNBT(partTag);
                    partData.put(side, PartStateHolder.of(part, partState));
                } else {
                    IntegratedDynamics.clog(Level.WARN, String.format("The part %s at position %s was at an invalid " +
                                    "side and removed.",
                            type, getPosition()));
                }
            } else {
                IntegratedDynamics.clog(Level.WARN, String.format("The part %s at position %s was unknown and removed.",
                        partTag.getString("__partType"), getPosition()));
            }
        }
    }

    @Override
    public DimPos getPosition() {
        return DimPos.of(getWorld(), getPos());
    }

    @Override
    public Map<EnumFacing, IPart<?, ?>> getParts() {
        return Maps.transformValues(partData, new Function<PartStateHolder<?, ?>, IPart<?, ?>>() {
            @Nullable
            @Override
            public IPart<?, ?> apply(@Nullable PartStateHolder<?, ?> input) {
                return input.getPart();
            }
        });
    }

    @Override
    public void setPart(EnumFacing side, IPart part) {
        partData.put(side, PartStateHolder.of(part, part.getDefaultState()));
    }

    @Override
    public IPart getPart(EnumFacing side) {
        return partData.get(side).getPart();
    }

    @Override
    public boolean hasPart(EnumFacing side) {
        return partData.containsKey(side);
    }

    @Override
    public IPart removePart(EnumFacing side) {
        return partData.remove(side).getPart();
    }

    @Override
    public  void setPartState(EnumFacing side, IPartState partState) {
        PartStateHolder<?, ?> partStateHolder = partData.get(side);
        if(partStateHolder == null) {
            throw new IllegalArgumentException(String.format("No part at position %s was found to update the state " +
                    "for.", getPosition()));
        }
        partData.put(side, PartStateHolder.of(partStateHolder.getPart(), partState));
    }

    @Override
    public IPartState getPartState(EnumFacing side) {
        PartStateHolder<?, ?> partStateHolder = partData.get(side);
        if(partStateHolder == null) {
            throw new IllegalArgumentException(String.format("No part at position %s was found to get the state from.",
                    getPosition()));
        }
        return partStateHolder.getState();
    }

    @Data
    private static class PartStateHolder<P extends IPart<P, S>, S extends IPartState<P>> {

        private final IPart<P, S> part;
        private final S state;

        public static PartStateHolder<?, ?> of(IPart part, IPartState partState) {
            return new PartStateHolder(part, partState);
        }

    }

}
