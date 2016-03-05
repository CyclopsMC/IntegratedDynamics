package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;

/**
 * A list proxy for a tank's capacities at a certain position.
 */
public class ValueTypeListProxyPositionedTankCapacities extends ValueTypeListProxyBase<ValueTypeInteger, ValueTypeInteger.ValueInteger> implements INBTProvider {

    @NBTPersist
    private DimPos pos;
    @NBTPersist
    private EnumFacing side;

    public ValueTypeListProxyPositionedTankCapacities() {
        this(null, null);
    }

    public ValueTypeListProxyPositionedTankCapacities(DimPos pos, EnumFacing side) {
        super(ValueTypeListProxyFactories.POSITIONED_TANK_CAPACITIES.getName(), ValueTypes.INTEGER);
        this.pos = pos;
        this.side = side;
    }

    protected IFluidHandler getTank() {
        return TileHelpers.getSafeTile(pos.getWorld(), pos.getBlockPos(), IFluidHandler.class);
    }

    @Override
    public int getLength() {
        IFluidHandler tank = getTank();
        if(tank == null) {
            return 0;
        }
        FluidTankInfo[] tanks = tank.getTankInfo(side);
        if(tanks == null) {
            return 0;
        }
        return tanks.length;
    }

    @Override
    public ValueTypeInteger.ValueInteger get(int index) {
        return ValueTypeInteger.ValueInteger.of(getTank().getTankInfo(side)[index].capacity);
    }

    @Override
    public void writeGeneratedFieldsToNBT(NBTTagCompound tag) {

    }

    @Override
    public void readGeneratedFieldsFromNBT(NBTTagCompound tag) {

    }
}
