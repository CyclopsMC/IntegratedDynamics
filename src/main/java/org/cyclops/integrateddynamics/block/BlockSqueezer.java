package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockContainer;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.tileentity.TileSqueezer;

import java.util.List;

/**
 * A block for squeezing stuff.
 * @author rubensworks
 */
public class BlockSqueezer extends ConfigurableBlockContainer {

    @BlockProperty
    public static final PropertyEnum<BlockSqueezer.EnumAxis> AXIS = PropertyDirection.create("axis", BlockSqueezer.EnumAxis.class);
    @BlockProperty
    public static final PropertyInteger HEIGHT = PropertyInteger.create("height", 1, 7); // 1 is heighest, 7 is lowest

    private static BlockSqueezer _instance = null;

    /**
     * Get the unique instance.
     *
     * @return The instance.
     */
    public static BlockSqueezer getInstance() {
        return _instance;
    }

    /**
     * Make a new block instance.
     *
     * @param eConfig Config for this block.
     */
    public BlockSqueezer(ExtendedConfig eConfig) {
        super(eConfig, Material.iron, TileSqueezer.class);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos blockPos, IBlockState blockState, EntityPlayer player, EnumFacing side, float motionX, float motionY, float motionZ) {
        if (world.isRemote) {
            return true;
        } else if(world.getBlockState(blockPos).getValue(BlockSqueezer.HEIGHT) == 1) {
            ItemStack itemStack = player.inventory.getCurrentItem();
            TileSqueezer tile = TileHelpers.getSafeTile(world, blockPos, TileSqueezer.class);
            if (tile != null) {
                if (itemStack == null && tile.getStackInSlot(0) != null) {
                    player.inventory.setInventorySlotContents(player.inventory.currentItem, tile.getStackInSlot(0));
                    tile.setInventorySlotContents(0, null);
                    tile.sendUpdate();
                    return true;
                } else if (itemStack != null && tile.getStackInSlot(0) == null) {
                    tile.setInventorySlotContents(0, itemStack.splitStack(1));
                    if (itemStack.stackSize <= 0)
                        player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                    tile.sendUpdate();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onLanded(World worldIn, Entity entityIn) {
        double motionY = entityIn.motionY;
        super.onLanded(worldIn, entityIn);
        if(!worldIn.isRemote && motionY <= -0.37D && entityIn instanceof EntityLivingBase) {
            // Same way of deriving blockPos as is done in Entity#moveEntity
            int i = MathHelper.floor_double(entityIn.posX);
            int j = MathHelper.floor_double(entityIn.posY - 0.2D);
            int k = MathHelper.floor_double(entityIn.posZ);
            BlockPos blockPos = new BlockPos(i, j, k);
            IBlockState blockState = worldIn.getBlockState(blockPos);

            // The faster the entity is falling, the more steps to advance by
            int steps = 1 + MathHelper.floor_double((-motionY - 0.37D) * 5);

            if((entityIn.posY - blockPos.getY()) - getRelativeTopPositionTop(worldIn, blockPos, blockState) <= 0.1F) {
                if (blockState.getBlock() == this) { // Just to be sure...
                    int newHeight = Math.min(7, blockState.getValue(HEIGHT) + steps);
                    worldIn.setBlockState(blockPos, blockState.withProperty(HEIGHT, newHeight));
                    TileSqueezer tile = TileHelpers.getSafeTile(worldIn, blockPos, TileSqueezer.class);
                    tile.setItemHeight(Math.max(newHeight, tile.getItemHeight()));
                }
            }
        }
    }

    @Override
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
        super.onNeighborBlockChange(worldIn, pos, state, neighborBlock);
        if(!worldIn.isRemote) {
            for (EnumFacing enumfacing : EnumFacing.values()) {
                if (worldIn.isSidePowered(pos.offset(enumfacing), enumfacing)) {
                    worldIn.setBlockState(pos, state.withProperty(HEIGHT, 1));
                    for(Entity entity : worldIn.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos, pos.add(1, 1, 1)))) {
                        entity.motionY += 0.25F;
                        entity.posY += 0.5F;
                    }
                    return;
                }
            }
        }
    }

    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        EnumFacing.Axis axis = placer.getHorizontalFacing().getAxis();
        return super.onBlockPlaced(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(AXIS, BlockSqueezer.EnumAxis.fromFacingAxis(axis));
    }

    public float getRelativeTopPositionTop(IBlockAccess world, BlockPos blockPos, IBlockState blockState) {
        return (9 - blockState.getValue(HEIGHT)) * 0.125F;
    }

    @Override
    public void addCollisionBoxesToList(World world, BlockPos blockPos, IBlockState blockState, AxisAlignedBB area, List<AxisAlignedBB> collisionBoxes, Entity entity) {
        // Bottom
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
        super.addCollisionBoxesToList(world, blockPos, blockState, area, collisionBoxes, entity);
        float f = 0.125F;

        // Sticks
        this.setBlockBounds(0.0F, 0.0F, 0.0F, f, 1.0F, f);
        super.addCollisionBoxesToList(world, blockPos, blockState, area, collisionBoxes, entity);
        this.setBlockBounds(1.0F, 0.0F, 0.0F, 1.0F - f, 1.0F, f);
        super.addCollisionBoxesToList(world, blockPos, blockState, area, collisionBoxes, entity);
        this.setBlockBounds(0.0F, 0.0F, 1.0F, f, 1.0F, 1.0F - f);
        super.addCollisionBoxesToList(world, blockPos, blockState, area, collisionBoxes, entity);
        this.setBlockBounds(1.0F, 0.0F, 1.0F, 1.0F - f, 1.0F, 1.0F - f);
        super.addCollisionBoxesToList(world, blockPos, blockState, area, collisionBoxes, entity);

        // Dynamic top
        float offset = (8 - blockState.getValue(HEIGHT)) * 0.125F;
        this.setBlockBounds(0.0F, offset - 0.125F, 0.0F, 1.0F, offset, 1.0F);
        super.addCollisionBoxesToList(world, blockPos, blockState, area, collisionBoxes, entity);

        setBlockBoundsBasedOnState(world, blockPos);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
        IBlockState state = worldIn.getBlockState(pos);
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, this.getRelativeTopPositionTop(worldIn, pos, state), 1.0F);
    }

    @Override
    public void setBlockBoundsForItemRender() {
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean isNormalCube() {
        return false;
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return true;
    }

    @Override
    public int getComparatorInputOverride(World world, BlockPos blockPos) {
        IBlockState blockState = world.getBlockState(blockPos);
        return (int) (((double) blockState.getValue(HEIGHT) - 1) / 6D * 15D);
    }

    public static enum EnumAxis implements IStringSerializable {
        X("x", new EnumFacing[]{EnumFacing.EAST, EnumFacing.WEST}),
        Z("z", new EnumFacing[]{EnumFacing.NORTH, EnumFacing.SOUTH});

        private final String name;
        private final EnumFacing[] sides;

        private EnumAxis(String name, EnumFacing[] sides) {
            this.name = name;
            this.sides = sides;
        }

        public String toString() {
            return this.name;
        }

        public static BlockSqueezer.EnumAxis fromFacingAxis(EnumFacing.Axis axis) {
            switch (axis) {
                case X:
                    return Z;
                case Z:
                    return X;
                default:
                    return X;
            }
        }

        public String getName() {
            return this.name;
        }

        public EnumFacing[] getSides() {
            return sides;
        }
    }
}
