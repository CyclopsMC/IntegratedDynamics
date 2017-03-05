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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockContainer;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.cyclopscore.recipe.custom.api.IMachine;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipeRegistry;
import org.cyclops.cyclopscore.recipe.custom.api.ISuperRecipeRegistry;
import org.cyclops.cyclopscore.recipe.custom.component.DummyPropertiesComponent;
import org.cyclops.cyclopscore.recipe.custom.component.ItemAndFluidStackRecipeComponent;
import org.cyclops.cyclopscore.recipe.custom.component.ItemStackRecipeComponent;
import org.cyclops.integrateddynamics.Achievements;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.tileentity.TileSqueezer;

import java.util.List;

/**
 * A block for squeezing stuff.
 * @author rubensworks
 */
public class BlockSqueezer extends ConfigurableBlockContainer implements IMachine<BlockSqueezer, ItemStackRecipeComponent, ItemAndFluidStackRecipeComponent, DummyPropertiesComponent> {

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
        super(eConfig, Material.IRON, TileSqueezer.class);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos blockPos, IBlockState blockState, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float motionX, float motionY, float motionZ) {
        if (world.isRemote) {
            return true;
        } else if(world.getBlockState(blockPos).getValue(BlockSqueezer.HEIGHT) == 1) {
            TileSqueezer tile = TileHelpers.getSafeTile(world, blockPos, TileSqueezer.class);
            if (tile != null) {
                ItemStack itemStack = player.inventory.getCurrentItem();
                ItemStack tileStack = tile.getStackInSlot(0);

                if (itemStack == null && tileStack != null) {
                    player.inventory.setInventorySlotContents(player.inventory.currentItem, tileStack);
                    tile.setInventorySlotContents(0, null);
                    tile.sendUpdate();
                    return true;
                } else if(player.inventory.addItemStackToInventory(tileStack)){
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

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block neighborBlock) {
        super.neighborChanged(state, worldIn, pos, neighborBlock);
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

    @SuppressWarnings("deprecation")
    @Override
    public void addCollisionBoxToList(IBlockState blockState, World world, BlockPos blockPos, AxisAlignedBB area, List<AxisAlignedBB> collisionBoxes, Entity entity) {
        // Bottom
        BlockHelpers.addCollisionBoxToList(blockPos, area, collisionBoxes, new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F));

        // Sticks
        float f = 0.125F;
        BlockHelpers.addCollisionBoxToList(blockPos, area, collisionBoxes, new AxisAlignedBB(0.0F, 0.0F, 0.0F, f, 1.0F, f));
        BlockHelpers.addCollisionBoxToList(blockPos, area, collisionBoxes, new AxisAlignedBB(1.0F, 0.0F, 0.0F, 1.0F - f, 1.0F, f));
        BlockHelpers.addCollisionBoxToList(blockPos, area, collisionBoxes, new AxisAlignedBB(0.0F, 0.0F, 1.0F, f, 1.0F, 1.0F - f));
        BlockHelpers.addCollisionBoxToList(blockPos, area, collisionBoxes, new AxisAlignedBB(1.0F, 0.0F, 1.0F, 1.0F - f, 1.0F, 1.0F - f));

        // Dynamic top
        float offset = (8 - blockState.getValue(HEIGHT)) * 0.125F;
        BlockHelpers.addCollisionBoxToList(blockPos, area, collisionBoxes, new AxisAlignedBB(0.0F, offset - 0.125F, 0.0F, 1.0F, offset, 1.0F));
    }

    @SuppressWarnings("deprecation")
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, this.getRelativeTopPositionTop(world, pos, state), 1.0F);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(IBlockState blockState) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState blockState, IBlockAccess world, BlockPos pos) {
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
        return (int) (((double) blockState.getValue(HEIGHT) - 1) / 6D * 15D);
    }

    @Override
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return side == EnumFacing.DOWN;
    }

    @Override
    public IRecipeRegistry<BlockSqueezer, ItemStackRecipeComponent, ItemAndFluidStackRecipeComponent, DummyPropertiesComponent> getRecipeRegistry() {
        return IntegratedDynamics._instance.getRegistryManager().getRegistry(ISuperRecipeRegistry.class).getRecipeRegistry(this);
    }

    @SubscribeEvent
    public void onCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.crafting.getItem() == Item.getItemFromBlock(this)) {
            event.player.addStat(Achievements.SQUEEZING);
        }
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
