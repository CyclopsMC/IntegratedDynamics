package org.cyclops.integrateddynamics.modcompat.tesla;

import net.darkhax.tesla.api.ITeslaConsumer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ModAPIManager;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.Reference;

/**
 * Helpers related to the Tesla API.
 * @author rubensworks
 */
public class TeslaHelpers {

    /**
     * @return If the RF API is available.
     */
    public static boolean isTesla() {
        return ModAPIManager.INSTANCE.hasAPI(Reference.MOD_TESLA_API);
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
            ITeslaConsumer consumer = TileHelpers.getCapability(world, pos.offset(side), side.getOpposite(), Capabilities.TESLA_CONSUMER);
            if(consumer != null) {
                toFill -= consumer.givePower(toFill, simulate);
                if(toFill <= 0) {
                    return energy;
                }
            }
        }
        return energy - toFill;
    }

}
