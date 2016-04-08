package org.cyclops.integrateddynamics.api.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Facade for a {@link IEnergyBattery} at a certain position.
 * Must be implemented on blocks.
 * @author rubensworks
 */
public interface IEnergyBatteryFacade {

    /**
     * Get the energy battery at a given position.
     * @param world The world.
     * @param pos The position.
     * @return The variable container.
     */
    public IEnergyBattery getEnergyBattery(World world, BlockPos pos);

}
