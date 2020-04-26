package org.cyclops.integrateddynamics.core.block;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.integrateddynamics.core.tileentity.TileMechanicalMachine;

import java.util.function.Supplier;

/**
 * A mechanical machine base block
 * @author rubensworks
 */
public abstract class BlockMechanicalMachine extends BlockTileGuiCabled {

    public static final String NBT_ENERGY = "energy";

    public BlockMechanicalMachine(Properties properties, Supplier<CyclopsTileEntity> tileEntitySupplier) {
        super(properties, tileEntitySupplier);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos blockPos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (!world.isRemote()) {
            TileHelpers.getSafeTile(world, blockPos, TileMechanicalMachine.class)
                    .ifPresent(tile -> {
                        if (itemStack.hasTag() && itemStack.getTag().contains(NBT_ENERGY, Constants.NBT.TAG_INT)) {
                            tile.setEnergy(itemStack.getTag().getInt(NBT_ENERGY));
                        }
                    });
        }
        super.onBlockPlacedBy(world, blockPos, state, placer, itemStack);
    }

}
