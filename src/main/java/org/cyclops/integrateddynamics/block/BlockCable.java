package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.client.icon.Icon;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockContainer;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.client.model.CableModel;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;

import java.util.List;

/**
 * A block that is build up from different parts.
 * This block refers to a ticking tile entity.
 * @author rubensworks
 */
public class BlockCable extends ConfigurableBlockContainer implements ICableConnectable {

    @BlockProperty
    public static final IUnlistedProperty<Boolean>[] CONNECTED = new IUnlistedProperty[6];
    static {
        for(EnumFacing side : EnumFacing.values()) {
            CONNECTED[side.ordinal()] = Properties.toUnlisted(PropertyBool.create(side.getName()));
        }
    }

    private static BlockCable _instance = null;

    private static final float[][] COLLISION_BOXES = {
            {CableModel.MIN, 0, CableModel.MIN, CableModel.MAX, CableModel.MAX, CableModel.MAX}, // DOWN
            {CableModel.MIN, CableModel.MIN, CableModel.MIN, CableModel.MAX, 1, CableModel.MAX}, // UP
            {CableModel.MIN, CableModel.MIN, 0, CableModel.MAX, CableModel.MAX, CableModel.MAX}, // NORTH
            {CableModel.MIN, 0, CableModel.MIN, CableModel.MAX, CableModel.MAX, 1}, // SOUTH
            {0, CableModel.MIN, CableModel.MIN, CableModel.MAX, CableModel.MAX, CableModel.MAX}, // WEST
            {CableModel.MIN, CableModel.MIN, CableModel.MIN, 1, CableModel.MAX, CableModel.MAX}, // EAST
    };

    @Icon(location = "blocks/cable")
    public TextureAtlasSprite texture;

    /**
     * Get the unique instance.
     * @return The instance.
     */
    public static BlockCable getInstance() {
        return _instance;
    }

    /**
     * Make a new block instance.
     * @param eConfig Config for this block.
     */
    public BlockCable(ExtendedConfig eConfig) {
        super(eConfig, Material.glass, TileMultipartTicking.class);

        setHardness(3.0F);
        setStepSound(soundTypeMetal);
        eConfig.getMod().getIconProvider().registerIconHolderObject(this);
    }

    @Override
    public int getRenderType() {
        return 3;
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return ((TileMultipartTicking) world.getTileEntity(pos)).getConnectionState();
    }

    @Override
    public IExtendedBlockState updateConnections(World world, BlockPos pos) {
        System.out.println("Updating at " + pos + " AT " + MinecraftHelpers.isClientSide());
        TileMultipartTicking tile = (TileMultipartTicking) world.getTileEntity(pos);
        if(tile != null) {
            IExtendedBlockState extendedState = (IExtendedBlockState) getDefaultState();
            for(EnumFacing side : EnumFacing.VALUES) {
                BlockPos neighbourPos = pos.offset(side);
                Block neighbourBlock = world.getBlockState(neighbourPos).getBlock();
                if(neighbourBlock instanceof ICableConnectable &&
                        ((ICableConnectable) neighbourBlock).canConnect(world, neighbourPos, this, pos)) {
                    extendedState = extendedState.withProperty(CONNECTED[side.ordinal()], true);
                }
            }
            tile.setConnectionState(extendedState);
            world.markBlockRangeForRenderUpdate(pos, pos);
            return extendedState;
        }
        return null;
    }

    protected void requestConnectionsUpdate(World world, BlockPos pos) {
        TileMultipartTicking tile = (TileMultipartTicking) world.getTileEntity(pos);
        if(tile != null) {
            tile.setConnectionState(null);
        }
        world.markBlockRangeForRenderUpdate(pos, pos);
    }


    protected void triggerNeighbourConnections(World world, BlockPos blockPos) {
        for(EnumFacing side : EnumFacing.VALUES) {
            requestConnectionsUpdate(world, blockPos.offset(side));
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack itemStack) {
        super.onBlockPlacedBy(world, pos, state, placer, itemStack);
        triggerNeighbourConnections(world, pos);
    }

    @Override
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
        super.onNeighborBlockChange(world, pos, state, neighborBlock);
    }

    @Override
    protected void onPostBlockDestroyed(World world, BlockPos blockPos) {
        triggerNeighbourConnections(world, blockPos);
        super.onPostBlockDestroyed(world, blockPos);
    }

    @Override
    public boolean hasDynamicModel() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IBakedModel createDynamicModel() {
        return new CableModel();
    }

    @Override
    public boolean canConnect(World world, BlockPos selfPosition, ICableConnectable connector, BlockPos otherPosition) {
        return true;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean isFullCube() {
        return false;
    }

    @Override
    public boolean isNormalCube() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public EnumWorldBlockLayer getBlockLayer() {
        return EnumWorldBlockLayer.TRANSLUCENT;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void addCollisionBoxesToList(World world, BlockPos pos, IBlockState state, AxisAlignedBB axisalignedbb, List list, Entity collidingEntity) {
        setBlockBounds(CableModel.MIN, CableModel.MIN, CableModel.MIN,
                       CableModel.MAX, CableModel.MAX, CableModel.MAX);
        super.addCollisionBoxesToList(world, pos, state, axisalignedbb, list, collidingEntity);
        IExtendedBlockState extendedState = (IExtendedBlockState) getExtendedState(state, world, pos);
        for(EnumFacing side : EnumFacing.values()) {
            if(extendedState != null && extendedState.getValue(CONNECTED[side.ordinal()]) != null) {
                if(extendedState.getValue(BlockCable.CONNECTED[side.ordinal()])) {
                    float[] b = COLLISION_BOXES[side.ordinal()];
                    setBlockBounds(b[0], b[1], b[2], b[3], b[4], b[5]);
                    super.addCollisionBoxesToList(world, pos, state, axisalignedbb, list, collidingEntity);
                }
            }
        }
    }

}
