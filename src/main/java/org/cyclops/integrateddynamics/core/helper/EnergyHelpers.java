package org.cyclops.integrateddynamics.core.helper;

import com.google.common.collect.Lists;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.cyclops.cyclopscore.helper.TileHelpers;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Helpers related to energy.
 * @author rubensworks
 */
public class EnergyHelpers {

    private static final List<IEnergyStorageProxy> ENERGY_STORAGE_PROXIES = Lists.newArrayList();

    public static void addEnergyStorageProxy(IEnergyStorageProxy energyStorageProxy) {
        ENERGY_STORAGE_PROXIES.add(energyStorageProxy);
    }

    public static IEnergyStorage getEnergyStorage(IBlockAccess world, BlockPos pos, EnumFacing facing) {
        IEnergyStorage energyStorage = TileHelpers.getCapability(world, pos, facing, CapabilityEnergy.ENERGY);
        if (energyStorage == null) {
            for (IEnergyStorageProxy energyStorageProxy : ENERGY_STORAGE_PROXIES) {
                energyStorage = energyStorageProxy.getEnergyStorageProxy(world, pos, facing);
                if (energyStorage != null) {
                    return energyStorage;
                }
            }

        }
        return energyStorage;
    }

    /**
     * Attempty to fill the neighbouring tiles with energy.
     * @param world The world.
     * @param pos The filler's position.
     * @param energy The energy to add.
     * @param simulate If the filling should be simulated.
     * @return The amount of energy that was filled somewhere.
     */
    public static int fillNeigbours(World world, BlockPos pos, int energy, boolean simulate) {
        int toFill = energy;
        for(EnumFacing side : EnumFacing.VALUES) {
            IEnergyStorage energyStorage = getEnergyStorage(world, pos.offset(side), side.getOpposite());
            if(energyStorage != null) {
                toFill -= energyStorage.receiveEnergy(toFill, simulate);
                if(toFill <= 0) {
                    return energy;
                }
            }
        }
        return energy - toFill;
    }

    public static interface IEnergyStorageProxy {
        public @Nullable IEnergyStorage getEnergyStorageProxy(IBlockAccess world, BlockPos pos, EnumFacing facing);
    }

}
