package org.cyclops.integrateddynamics.modcompat.rf;

import cofh.api.energy.IEnergyReceiver;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ModAPIManager;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.Reference;

/**
 * Helpers related to the RF API.
 * @author rubensworks
 */
public class RfHelpers {

    /**
     * @return If the RF API is available.
     */
    public static boolean isRf() {
        return ModAPIManager.INSTANCE.hasAPI(Reference.MOD_RF_API);
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
            IEnergyReceiver energyReceiver = TileHelpers.getSafeTile(world, pos.offset(side), IEnergyReceiver.class);
            if(energyReceiver != null) {
                toFill -= energyReceiver.receiveEnergy(side.getOpposite(), toFill, simulate);
                if(toFill <= 0) {
                    return energy;
                }
            }
        }
        return energy - toFill;
    }

}
