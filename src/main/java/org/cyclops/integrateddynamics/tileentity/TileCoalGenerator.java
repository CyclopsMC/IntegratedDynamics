package org.cyclops.integrateddynamics.tileentity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.datastructure.IntReferenceHolderSupplied;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.network.IEnergyNetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integrateddynamics.block.BlockCoalGenerator;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderConfig;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderSingleton;
import org.cyclops.integrateddynamics.core.helper.EnergyHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.tileentity.TileCableConnectableInventory;
import org.cyclops.integrateddynamics.inventory.container.ContainerCoalGenerator;
import org.cyclops.integrateddynamics.network.CoalGeneratorNetworkElement;

import javax.annotation.Nullable;

/**
 * A part entity for the coal energy generator.
 * @author rubensworks
 */
public class TileCoalGenerator extends TileCableConnectableInventory implements IEnergyStorage, INamedContainerProvider {

    public static final int INVENTORY_SIZE = 1;
    public static final int MAX_PROGRESS = 13;
    public static final int ENERGY_PER_TICK = 20;
    public static final int SLOT_FUEL = 0;

    @NBTPersist
    private int currentlyBurningMax;
    @NBTPersist
    private int currentlyBurning;

    public TileCoalGenerator() {
        super(RegistryEntries.TILE_ENTITY_COAL_GENERATOR, TileCoalGenerator.INVENTORY_SIZE, 64);
        addCapabilityInternal(NetworkElementProviderConfig.CAPABILITY, LazyOptional.of(() -> new NetworkElementProviderSingleton() {
            @Override
            public INetworkElement createNetworkElement(World world, BlockPos blockPos) {
                return new CoalGeneratorNetworkElement(DimPos.of(world, blockPos));
            }
        }));
        addCapabilityInternal(CapabilityEnergy.ENERGY, LazyOptional.of(() -> this));
    }

    public LazyOptional<IEnergyNetwork> getEnergyNetwork() {
        return NetworkHelpers.getEnergyNetwork(getNetwork());
    }

    public void updateBlockState() {
        boolean wasBurning = getWorld().getBlockState(getPos()).get(BlockCoalGenerator.LIT);
        boolean isBurning = isBurning();
        if (isBurning != wasBurning) {
            getWorld().setBlockState(getPos(),
                    getWorld().getBlockState(getPos()).with(BlockCoalGenerator.LIT, isBurning));
        }
    }

    public int getProgress() {
        float current = currentlyBurning;
        float max = currentlyBurningMax;
        if (max == 0) {
            return -1;
        }
        return Math.round((current / max) * (float) MAX_PROGRESS);
    }

    public boolean isBurning() {
        return currentlyBurning < currentlyBurningMax;
    }

    public boolean canAddEnergy(int energy) {
        IEnergyNetwork network = getEnergyNetwork().orElse(null);
        if(network != null && network.getChannel(IPositionedAddonsNetwork.DEFAULT_CHANNEL).insert(energy, true) == 0) {
            return true;
        }
        return addEnergyFe(energy, true) == energy;
    }

    protected int addEnergy(int energy) {
        IEnergyNetwork network = getEnergyNetwork().orElse(null);
        int toFill = energy;
        if(network != null) {
            toFill = network.getChannel(IPositionedAddonsNetwork.DEFAULT_CHANNEL).insert(toFill, false);
        }
        if(toFill > 0) {
            toFill -= addEnergyFe(toFill, false);
        }
        return energy - toFill;
    }

    protected int addEnergyFe(int energy, boolean simulate) {
        return EnergyHelpers.fillNeigbours(getWorld(), getPos(), energy, simulate);
    }

    public static int getFuelTime(ItemStack itemStack) {
        int ret = itemStack.getBurnTime();
        return net.minecraftforge.event.ForgeEventFactory.getItemBurnTime(itemStack, ret == -1
                ? AbstractFurnaceTileEntity.getBurnTimes().getOrDefault(itemStack.getItem(), 0) : ret);
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        if(!getWorld().isRemote && (!getInventory().getStackInSlot(SLOT_FUEL).isEmpty() || isBurning()) && canAddEnergy(ENERGY_PER_TICK)) {
            if (isBurning()) {
                if (currentlyBurning++ >= currentlyBurningMax) {
                    currentlyBurning = 0;
                    currentlyBurningMax = 0;
                }
                int toFill = ENERGY_PER_TICK;
                addEnergy(toFill);
                markDirty();
            }
            if (!isBurning()) {
                ItemStack fuel;
                if (getFuelTime(getInventory().getStackInSlot(SLOT_FUEL)) > 0
                        && !(fuel = getInventory().decrStackSize(SLOT_FUEL, 1)).isEmpty()) {
                    if(getInventory().getStackInSlot(SLOT_FUEL).isEmpty()) {
                        getInventory().setInventorySlotContents(SLOT_FUEL, fuel.getItem().getContainerItem(fuel));
                    }
                    currentlyBurningMax = getFuelTime(fuel);
                    currentlyBurning = 0;
                    markDirty();
                }
                updateBlockState();
            }
        }
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return 0;
    }

    @Override
    public int getMaxEnergyStored() {
        return 0;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("block.blocks.integrateddynamics.coal_generator");
    }

    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerCoalGenerator(id, playerInventory, this.getInventory(), new IntReferenceHolderSupplied(this::getProgress));
    }
}
