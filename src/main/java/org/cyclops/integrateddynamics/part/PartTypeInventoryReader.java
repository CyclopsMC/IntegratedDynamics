package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Sets;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import org.cyclops.integrateddynamics.block.InventoryReaderConfig;
import org.cyclops.integrateddynamics.core.block.IgnoredBlock;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.read.DefaultPartStateReader;
import org.cyclops.integrateddynamics.core.part.read.PartTypeReadBase;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * An inventory reader part.
 * @author rubensworks
 */
public class PartTypeInventoryReader extends PartTypeReadBase<PartTypeInventoryReader, DefaultPartStateReader<PartTypeInventoryReader>> {

    public PartTypeInventoryReader(String name) {
        super(name);
        AspectRegistry.getInstance().register(this, Sets.<IAspect>newHashSet(
                Aspects.READ_BOOLEAN_INVENTORY_FULL,
                Aspects.READ_BOOLEAN_INVENTORY_EMPTY,
                Aspects.READ_BOOLEAN_INVENTORY_NONEMPTY,
                Aspects.READ_BOOLEAN_INVENTORY_APPLICABLE,
                Aspects.READ_INTEGER_INVENTORY_COUNT
        ));
    }

    @Override
    public boolean isSolid(DefaultPartStateReader<PartTypeInventoryReader> state) {
        return true;
    }

    @Override
    public DefaultPartStateReader<PartTypeInventoryReader> constructDefaultState() {
        return new DefaultPartStateReader<PartTypeInventoryReader>();
    }

    @Override
    public int getUpdateInterval(DefaultPartStateReader<PartTypeInventoryReader> state) {
        return 10;
    }

    @Override
    public IBlockState getBlockState(TileMultipartTicking tile, double x, double y, double z, float partialTick,
                                     int destroyStage, EnumFacing side) {
        return InventoryReaderConfig._instance.getBlockInstance().getDefaultState().withProperty(IgnoredBlock.FACING, side);
    }

}
