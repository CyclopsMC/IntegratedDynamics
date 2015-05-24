package org.cyclops.integrateddynamics.part;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.client.gui.GuiPartReader;
import org.cyclops.integrateddynamics.core.part.*;
import org.cyclops.integrateddynamics.core.part.read.IPartTypeRedstoneReader;
import org.cyclops.integrateddynamics.core.part.write.IPartTypeRedstoneWriter;
import org.cyclops.integrateddynamics.inventory.container.ContainerPartReader;

/**
 * A redstone I/O part.
 * @author rubensworks
 */
public class PartTypeRedstone extends PartTypeBase<PartTypeRedstone, PartTypeRedstone.PartRedstoneState>
        implements
        IPartTypeRedstoneReader<PartTypeRedstone, PartTypeRedstone.PartRedstoneState>,
        IPartTypeRedstoneWriter<PartTypeRedstone, PartTypeRedstone.PartRedstoneState> {

    @Override
    protected boolean hasGui() {
        return true;
    }

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerPartReader.class;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Class<? extends GuiScreen> getGui() {
        return GuiPartReader.class;
    }

    @Override
    public EnumPartType getType() {
        return PartTypes.REDSTONE;
    }

    @Override
    public void toNBT(NBTTagCompound tag, PartRedstoneState partState) {
        partState.writeToNBT(tag);
    }

    @Override
    public PartRedstoneState fromNBT(NBTTagCompound tag) {
        PartRedstoneState partState = constructDefaultState();
        partState.readFromNBT(tag);
        return partState;
    }

    @Override
    public PartRedstoneState constructDefaultState() {
        return PartRedstoneState.of(0);
    }

    @Override
    public int getUpdateInterval(IPartState<PartTypeRedstone> state) {
        return 10;
    }

    @Override
    public boolean isUpdate(IPartState<PartTypeRedstone> state) {
        return true;
    }

    @Override
    public void update(IPartState<PartTypeRedstone> state) {
        
    }

    @Override
    public void beforeNetworkKill(IPartState<PartTypeRedstone> state) {
        System.out.println("killing " + state);
    }

    @Override
    public void afterNetworkAlive(IPartState<PartTypeRedstone> state) {
        System.out.println("alive " + state);
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

    @Override
    public ModBase getMod() {
        return IntegratedDynamics._instance;
    }

    @EqualsAndHashCode(callSuper = false)
    @Data(staticConstructor = "of")
    public static class PartRedstoneState extends DefaultPartState<PartTypeRedstone> {

        @NBTPersist
        @NonNull
        private int redstoneLevel;

    }

}
