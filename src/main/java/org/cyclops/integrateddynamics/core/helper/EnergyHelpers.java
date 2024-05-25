package org.cyclops.integrateddynamics.core.helper;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.integrateddynamics.api.part.PartPos;

import java.util.List;
import java.util.Optional;

/**
 * Helpers related to energy.
 * @author rubensworks
 */
public class EnergyHelpers {

    private static final List<IEnergyStorageProxy> ENERGY_STORAGE_PROXIES = Lists.newArrayList();

    public static void addEnergyStorageProxy(IEnergyStorageProxy energyStorageProxy) {
        ENERGY_STORAGE_PROXIES.add(energyStorageProxy);
    }

    public static Optional<IEnergyStorage> getEnergyStorage(PartPos pos) {
        return getEnergyStorage(pos.getPos(), pos.getSide());
    }

    public static Optional<IEnergyStorage> getEnergyStorage(DimPos pos, Direction facing) {
        Level world = pos.getLevel(true);
        return world != null ? getEnergyStorage(world, pos.getBlockPos(), facing) : Optional.empty();
    }

    public static Optional<IEnergyStorage> getEnergyStorage(Level world, BlockPos pos, Direction facing) {
        IEnergyStorage energyStorage = BlockEntityHelpers.getCapability(world, pos, facing, net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK)
                .orElseGet(() -> {
                    for (IEnergyStorageProxy energyStorageProxy : ENERGY_STORAGE_PROXIES) {
                        Optional<IEnergyStorage> optionalEnergyStorage = energyStorageProxy.getEnergyStorageProxy(world, pos, facing);
                        if (optionalEnergyStorage.isPresent()) {
                            return optionalEnergyStorage.orElse(null);
                        }
                    }
                    return null;
                });
        return energyStorage == null ? Optional.empty() : Optional.of(energyStorage);
    }

    /**
     * Attempty to fill the neighbouring tiles with energy.
     * @param world The world.
     * @param pos The filler's position.
     * @param energy The energy to add.
     * @param simulate If the filling should be simulated.
     * @return The amount of energy that was filled somewhere.
     */
    public static int fillNeigbours(Level world, BlockPos pos, int energy, boolean simulate) {
        int toFill = energy;
        for(Direction side : Direction.values()) {
            IEnergyStorage energyStorage = getEnergyStorage(world, pos.relative(side), side.getOpposite()).orElse(null);
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
        public Optional<IEnergyStorage> getEnergyStorageProxy(BlockGetter world, BlockPos pos, Direction facing);
    }

}
