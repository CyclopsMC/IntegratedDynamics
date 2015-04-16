package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
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
import org.cyclops.integrateddynamics.client.model.CableModel;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;

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

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack itemStack) {
        world.notifyNeighborsOfStateChange(pos, this);
        for(EnumFacing side : EnumFacing.VALUES) {
            requestConnectionsUpdate(world, pos.offset(side));
        }
        super.onBlockPlacedBy(world, pos, state, placer, itemStack);
    }

    @Override
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
        super.onNeighborBlockChange(world, pos, state, neighborBlock);
        if(neighborBlock instanceof ICableConnectable) {
            requestConnectionsUpdate(world, pos);
        }
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

}
