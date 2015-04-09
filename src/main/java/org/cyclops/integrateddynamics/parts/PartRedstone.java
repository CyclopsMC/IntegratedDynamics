package org.cyclops.integrateddynamics.parts;

import lombok.Data;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.core.parts.EnumPartType;
import org.cyclops.integrateddynamics.core.parts.IPartContainer;
import org.cyclops.integrateddynamics.core.parts.IPartState;
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
        System.out.println("WRITE");
        // TODO: abstract IPartState writing with parts of the TE NBTPersist annotation (this will require another abstraction for that writing).
    }

    @Override
    public PartRedstoneState fromNBT(NBTTagCompound tag) {
        System.out.println("READ");
        return getDefaultState(); // TODO: abstract IPartState reading
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

    @Data(staticConstructor = "of")
    public static class PartRedstoneState implements IPartState<PartRedstone> {

        private final int redstoneLevel;

    }

}
