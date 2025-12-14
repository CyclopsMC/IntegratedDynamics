package org.cyclops.integrateddynamics.core.helper;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.IModHelpersNeoForge;
import org.cyclops.integrateddynamics.api.part.PartPos;

import java.util.List;
import java.util.Optional;

/**
 * Helpers related to energy.
 * @author rubensworks
 */
public class EnergyHelpers {

    private static final List<EnergyHandlerProxy> ENERGY_STORAGE_PROXIES = Lists.newArrayList();

    public static void addEnergyStorageProxy(EnergyHandlerProxy energyStorageProxy) {
        ENERGY_STORAGE_PROXIES.add(energyStorageProxy);
    }

    public static Optional<EnergyHandler> getEnergyStorage(PartPos pos) {
        return getEnergyStorage(pos.getPos(), pos.getSide());
    }

    public static Optional<EnergyHandler> getEnergyStorage(DimPos pos, Direction facing) {
        Level world = pos.getLevel(true);
        return world != null ? getEnergyStorage(world, pos.getBlockPos(), facing) : Optional.empty();
    }

    public static Optional<EnergyHandler> getEnergyStorage(Level world, BlockPos pos, Direction facing) {
        EnergyHandler energyStorage = IModHelpersNeoForge.get().getCapabilityHelpers().getCapability(world, pos, facing, net.neoforged.neoforge.capabilities.Capabilities.Energy.BLOCK)
                .orElseGet(() -> {
                    for (EnergyHandlerProxy energyStorageProxy : ENERGY_STORAGE_PROXIES) {
                        Optional<EnergyHandler> optionalEnergyStorage = energyStorageProxy.getEnergyStorageProxy(world, pos, facing);
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
            EnergyHandler energyStorage = getEnergyStorage(world, pos.relative(side), side.getOpposite()).orElse(null);
            if(energyStorage != null) {
                try (var tx = Transaction.openRoot()) {
                    toFill -= energyStorage.insert(toFill, tx);
                    if (!simulate) {
                        tx.commit();
                    }
                }
                if(toFill <= 0) {
                    return energy;
                }
            }
        }
        return energy - toFill;
    }

    public static interface EnergyHandlerProxy {
        public Optional<EnergyHandler> getEnergyStorageProxy(BlockGetter world, BlockPos pos, Direction facing);
    }

}
