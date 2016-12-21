package org.cyclops.integrateddynamics.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockContainer;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.fluid.SingleUseTank;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.cyclopscore.helper.InventoryHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.cyclopscore.recipe.custom.api.IMachine;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipeRegistry;
import org.cyclops.cyclopscore.recipe.custom.api.ISuperRecipeRegistry;
import org.cyclops.cyclopscore.recipe.custom.component.DurationRecipeProperties;
import org.cyclops.cyclopscore.recipe.custom.component.ItemAndFluidStackRecipeComponent;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.tileentity.TileDryingBasin;

import java.util.List;

/**
 * A block for drying stuff.
 * @author rubensworks
 */
public class BlockDryingBasin extends ConfigurableBlockContainer implements IMachine<BlockDryingBasin, ItemAndFluidStackRecipeComponent, ItemAndFluidStackRecipeComponent, DurationRecipeProperties> {

    private static BlockDryingBasin _instance = null;

    /**
     * Get the unique instance.
     *
     * @return The instance.
     */
    public static BlockDryingBasin getInstance() {
        return _instance;
    }

    /**
     * Make a new block instance.
     *
     * @param eConfig Config for this block.
     */
    public BlockDryingBasin(ExtendedConfig eConfig) {
        super(eConfig, Material.WOOD, TileDryingBasin.class);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos blockPos, IBlockState blockState, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float motionX, float motionY, float motionZ) {
        if(world.isRemote) {
            return true;
        } else {
            ItemStack itemStack = player.inventory.getCurrentItem();
            TileDryingBasin tile = TileHelpers.getSafeTile(world, blockPos, TileDryingBasin.class);
            if (tile != null) {
                IFluidHandler itemFluidHandler = FluidUtil.getFluidHandler(itemStack);
                SingleUseTank tank = tile.getTank();
                ItemStack tileStack = tile.getStackInSlot(0);
                if ((itemStack == null || (ItemStack.areItemsEqual(itemStack, tileStack) && ItemStack.areItemStackTagsEqual(itemStack, tileStack) && itemStack.stackSize < itemStack.getMaxStackSize())) && tileStack != null) {
                    if(itemStack != null) {
                        tileStack.stackSize += itemStack.stackSize;
                    }
                    player.inventory.setInventorySlotContents(player.inventory.currentItem, tileStack);
                    tile.setInventorySlotContents(0, null);
                    tile.sendUpdate();
                    return true;
                } else if (itemFluidHandler != null && !tank.isFull()
                        && FluidUtil.tryEmptyContainer(itemStack, tank, Integer.MAX_VALUE, player, false) != null) {
                    ItemStack newItemStack = FluidUtil.tryEmptyContainer(itemStack, tank, Integer.MAX_VALUE, player, true);
                    InventoryHelpers.tryReAddToStack(player, itemStack, newItemStack);
                    tile.sendUpdate();
                    return true;
                } else if (itemFluidHandler != null && !tank.isEmpty() &&
                        FluidUtil.tryFillContainer(itemStack, tank, Integer.MAX_VALUE, player, false) != null) {
                    ItemStack newItemStack = FluidUtil.tryFillContainer(itemStack, tank, Integer.MAX_VALUE, player, true);
                    InventoryHelpers.tryReAddToStack(player, itemStack, newItemStack);
                    return true;
                } else if (itemStack != null && tileStack == null) {
                    tile.setInventorySlotContents(0, itemStack.splitStack(1));
                    if(itemStack.stackSize <= 0) player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                    tile.sendUpdate();
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos blockPos, AxisAlignedBB area, List<AxisAlignedBB> collisionBoxes, Entity entity) {
        float f = 0.125F;
        BlockHelpers.addCollisionBoxToList(blockPos, area, collisionBoxes, new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 0.3125F, 1.0F));
        BlockHelpers.addCollisionBoxToList(blockPos, area, collisionBoxes, new AxisAlignedBB(0.0F, 0.0F, 0.0F, f, 1.0F, 1.0F));
        BlockHelpers.addCollisionBoxToList(blockPos, area, collisionBoxes, new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, f));
        BlockHelpers.addCollisionBoxToList(blockPos, area, collisionBoxes, new AxisAlignedBB(1.0F - f, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F));
        BlockHelpers.addCollisionBoxToList(blockPos, area, collisionBoxes, new AxisAlignedBB(0.0F, 0.0F, 1.0F - f, 1.0F, 1.0F, 1.0F));
    }

    @SuppressWarnings("deprecation")
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return new AxisAlignedBB(0.03125F, 0.03125F, 0.03125F, 0.96875F, 0.96875F, 0.96875F);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isOpaqueCube(IBlockState blockState) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState blockState, IBlockAccess world, BlockPos blockPos) {
        return false;
    }
    
    @Override
    public boolean isFullCube(IBlockState blockState) {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasComparatorInputOverride(IBlockState blockState) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getComparatorInputOverride(IBlockState blockState, World world, BlockPos blockPos) {
        TileDryingBasin tile = TileHelpers.getSafeTile(world, blockPos, TileDryingBasin.class);
        if(tile == null) return 0;
        return tile.getInventory().getStackInSlot(0) != null ? 15 : 0;
    }

    @Override
    public IRecipeRegistry<BlockDryingBasin, ItemAndFluidStackRecipeComponent, ItemAndFluidStackRecipeComponent, DurationRecipeProperties> getRecipeRegistry() {
        return IntegratedDynamics._instance.getRegistryManager().getRegistry(ISuperRecipeRegistry.class).getRecipeRegistry(this);
    }

    @Override
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return side != EnumFacing.UP && side != EnumFacing.DOWN && super.isSideSolid(base_state, world, pos, side);
    }
}
