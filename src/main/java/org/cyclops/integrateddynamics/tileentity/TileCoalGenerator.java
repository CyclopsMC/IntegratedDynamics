package org.cyclops.integrateddynamics.tileentity;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.api.network.IEnergyNetwork;
import org.cyclops.integrateddynamics.block.BlockCoalGenerator;
import org.cyclops.integrateddynamics.core.tileentity.TileCableConnectableInventory;

/**
 * A tile entity for the coal energy generator.
 * @author rubensworks
 */
public class TileCoalGenerator extends TileCableConnectableInventory {

    public static final int MAX_PROGRESS = 13;
    public static final int ENERGY_PER_TICK = 20;
    protected static final int SLOT_FUEL = 0;

    @NBTPersist
    private int currentlyBurningMax;
    @NBTPersist
    private int currentlyBurning;

    public TileCoalGenerator() {
        super(1, "fuel", 64);
    }

    @Override
    public IEnergyNetwork getNetwork() {
        return (IEnergyNetwork) super.getNetwork();
    }

    public void updateBlockState() {
        getWorld().setBlockState(getPos(), getWorld().getBlockState(getPos()).withProperty(BlockCoalGenerator.ON, isBurning()));
    }

    public int getProgress() {
        float current = currentlyBurning;
        float max = currentlyBurningMax;
        return Math.round((current / max) * (float) MAX_PROGRESS);
    }

    public boolean isBurning() {
        return currentlyBurning < currentlyBurningMax;
    }

    protected boolean canAddEnergy(int energy) {
        IEnergyNetwork network = getNetwork();
        return network != null && network.addEnergy(energy, true) == energy;
    }

    protected void addEnergy(int energy) {
        getNetwork().addEnergy(energy, false);
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        if((getStackInSlot(SLOT_FUEL) != null || isBurning()) && canAddEnergy(ENERGY_PER_TICK)) {
            if (isBurning()) {
                if (currentlyBurning++ >= currentlyBurningMax) {
                    currentlyBurning = 0;
                    currentlyBurningMax = 0;
                    sendUpdate();
                }
                addEnergy(ENERGY_PER_TICK);
                markDirty();
            }
            if (!isBurning()) {
                ItemStack fuel;
                if ((fuel = decrStackSize(SLOT_FUEL, 1)) != null && TileEntityFurnace.isItemFuel(fuel)) {
                    if(getStackInSlot(SLOT_FUEL) == null) {
                        setInventorySlotContents(SLOT_FUEL, fuel.getItem().getContainerItem(fuel));
                    }
                    currentlyBurningMax = TileEntityFurnace.getItemBurnTime(fuel);
                    currentlyBurning = 0;
                    sendUpdate();
                    updateBlockState();
                }
            }
        }
    }
}
