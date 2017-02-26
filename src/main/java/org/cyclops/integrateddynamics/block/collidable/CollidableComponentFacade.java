package org.cyclops.integrateddynamics.block.collidable;

import com.google.common.base.Optional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.block.IFacadeable;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.capability.facadeable.FacadeableConfig;
import org.cyclops.integrateddynamics.core.block.ICollidable;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.item.ItemFacade;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Collidable component for facades.
 * @author rubensworks
 */
public class CollidableComponentFacade implements ICollidable.IComponent<EnumFacing, BlockCable> {

    private final AxisAlignedBB BOUNDS = new AxisAlignedBB(0.01, 0.01, 0.01, 0.99, 0.99, 0.99);

    @Override
    public Collection<EnumFacing> getPossiblePositions() {
        return Arrays.asList(new EnumFacing[]{null});
    }

    @Override
    public int getBoundsCount(EnumFacing position) {
        return 1;
    }

    @Override
    public boolean isActive(BlockCable block, World world, BlockPos pos, EnumFacing position) {
        return CableHelpers.hasFacade(world, pos);
    }

    @Override
    public List<AxisAlignedBB> getBounds(BlockCable block, World world, BlockPos pos, EnumFacing position) {
        return Collections.singletonList(BOUNDS);
    }

    @Override
    public ItemStack getPickBlock(World world, BlockPos pos, EnumFacing position) {
        ItemStack itemStack = new ItemStack(ItemFacade.getInstance());
        ItemFacade.getInstance().writeFacadeBlock(itemStack, CableHelpers.getFacade(world, pos));
        return itemStack;
    }

    @Override
    public boolean destroy(World world, BlockPos pos, EnumFacing position, EntityPlayer player) {
        if(!world.isRemote) {
            IFacadeable facadeable = TileHelpers.getCapability(world, pos, null, FacadeableConfig.CAPABILITY);
            IBlockState blockState = facadeable.getFacade();
            ItemStack itemStack = new ItemStack(ItemFacade.getInstance());
            ItemFacade.getInstance().writeFacadeBlock(itemStack, blockState);
            facadeable.setFacade(null);
            if (!player.capabilities.isCreativeMode) {
                ItemStackHelpers.spawnItemStackToPlayer(world, pos, itemStack, player);
            }
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    @SideOnly(Side.CLIENT)
    public IBakedModel getBreakingBaseModel(World world, BlockPos pos, EnumFacing position) {
        IBlockState blockState = world.getBlockState(pos);
        IExtendedBlockState state = (IExtendedBlockState) blockState.getBlock().getExtendedState(blockState, world, pos);
        Optional<IBlockState> blockStateOptional = BlockHelpers.getSafeBlockStateProperty(state, BlockCable.FACADE, Optional.absent());
        if(!blockStateOptional.isPresent()) return null;
        return RenderHelpers.getBakedModel(blockStateOptional.get());
    }

}
