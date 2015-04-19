package org.cyclops.integrateddynamics.parts;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.core.parts.DefaultPartState;
import org.cyclops.integrateddynamics.core.parts.EnumPartType;
import org.cyclops.integrateddynamics.core.parts.IPartContainer;
import org.cyclops.integrateddynamics.core.parts.read.IPartRedstoneReader;
import org.cyclops.integrateddynamics.core.parts.write.IPartRedstoneWriter;

/**
 * A redstone I/O part.
 * @author rubensworks
 */
public class PartRedstone implements IPartRedstoneReader<PartRedstone, PartRedstone.PartRedstoneState>,
        IPartRedstoneWriter<PartRedstone, PartRedstone.PartRedstoneState> {

    @Override
    public EnumPartType getType() {
        return Parts.REDSTONE;
    }

    @Override
    public void toNBT(NBTTagCompound tag, PartRedstoneState partState) {
        partState.writeToNBT(tag);
    }

    @Override
    public PartRedstoneState fromNBT(NBTTagCompound tag) {
        PartRedstoneState partState = getDefaultState();
        partState.readFromNBT(tag);
        return partState;
    }

    @Override
    public PartRedstoneState getDefaultState() {
        return PartRedstoneState.of(0);
    }

    @Override
    public int getRedstoneLevel(IPartContainer partContainer, EnumFacing side) {
        DimPos dimPos = partContainer.getPosition();
        return dimPos.getWorld().getRedstonePower(dimPos.getBlockPos(), side);
    }

    @Override
    public void setRedstoneLevel(IPartContainer partContainer, EnumFacing side, int level) {
        partContainer.setPartState(side, PartRedstoneState.of(level));
    }

    @EqualsAndHashCode(callSuper = false)
    @Data(staticConstructor = "of")
    public static class PartRedstoneState extends DefaultPartState<PartRedstone> {

        @NBTPersist
        @NonNull
        private int redstoneLevel;

    }

}
