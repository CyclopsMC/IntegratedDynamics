package org.cyclops.integrateddynamics.tileentity;

import com.google.common.collect.Sets;
import lombok.experimental.Delegate;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidContainerRegistry;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.cyclopscore.tileentity.TankInventoryTileEntity;
import org.cyclops.integrateddynamics.fluid.FluidMenrilResin;

/**
 * A tile entity for drying stuff.
 * @author rubensworks
 */
public class TileDryingBasin extends TankInventoryTileEntity implements CyclopsTileEntity.ITickingTile {

    @Delegate
    private final ITickingTile tickingTileComponent = new TickingTileComponent(this);

    @NBTPersist
    private Float randomRotation = 0F;

    public TileDryingBasin() {
        super(1, "dryingBasingInventory", FluidContainerRegistry.BUCKET_VOLUME, "dryingBasingTank", FluidMenrilResin.getInstance());

        addSlotsToSide(EnumFacing.UP, Sets.newHashSet(1));
        addSlotsToSide(EnumFacing.DOWN, Sets.newHashSet(1));
        addSlotsToSide(EnumFacing.NORTH, Sets.newHashSet(1));
        addSlotsToSide(EnumFacing.SOUTH, Sets.newHashSet(1));
        addSlotsToSide(EnumFacing.WEST, Sets.newHashSet(1));
        addSlotsToSide(EnumFacing.EAST, Sets.newHashSet(1));
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
    }

    @Override
    public void setInventorySlotContents(int slotId, ItemStack itemstack) {
        super.setInventorySlotContents(slotId, itemstack);
        this.randomRotation = worldObj.rand.nextFloat() * 360;
    }

    /**
     * Get the random rotation for displaying the item.
     * @return The random rotation.
     */
    public float getRandomRotation() {
        return randomRotation;
    }
}
