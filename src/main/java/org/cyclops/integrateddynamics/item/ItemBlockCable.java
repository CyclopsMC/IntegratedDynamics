package org.cyclops.integrateddynamics.item;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.block.cable.ICableFakeable;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The item for the cable.
 * @author rubensworks
 */
public class ItemBlockCable extends BlockItem {

    private static final List<IUseAction> USE_ACTIONS = Lists.newArrayList();

    public ItemBlockCable(Block block, Item.Properties builder) {
        super(block, builder);
    }

    /**
     * Register a use action for the cable item.
     * @param useAction The use action.
     */
    public static void addUseAction(IUseAction useAction) {
        USE_ACTIONS.add(useAction);
    }

    protected boolean checkCableAt(World world, BlockPos pos, @Nullable Direction side) {
        if (!CableHelpers.isNoFakeCable(world, pos, side) && CableHelpers.getCable(world, pos, side) != null) {
            return true;
        }
        for (IUseAction useAction : USE_ACTIONS) {
            if (useAction.canPlaceAt(world, pos)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean canPlace(BlockItemUseContext context, BlockState blockState) {
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        BlockPos target = pos.relative(side);
        // First check if the target is an unreal cable.
        if(checkCableAt(world, pos, side)) return true;
        // Then check if the target is covered by an unreal cable at the given side.
        if(checkCableAt(world, target, side.getOpposite())) return true;
        // Skips client-side entity collision detection for placing cables.
        return (!this.mustSurvive() || blockState.canSurvive(context.getLevel(), target));
    }

    protected boolean attempItemUseTarget(ItemUseContext context, BlockPos pos, Direction side, BlockCable blockCable, boolean offsetAdded) {
        BlockState blockState = context.getLevel().getBlockState(pos);
        Block block = blockState.getBlock();
        if(!block.isAir(blockState, context.getLevel(), pos)) {
            ICableFakeable cable = CableHelpers.getCableFakeable(context.getLevel(), pos, side).orElse(null);
            if (cable != null && !cable.isRealCable()) {
                if (!context.getLevel().isClientSide()) {
                    cable.setRealCable(true);
                    CableHelpers.updateConnections(context.getLevel(), pos, side);
                    CableHelpers.onCableAdded(context.getLevel(), pos);
                    CableHelpers.onCableAddedByPlayer(context.getLevel(), pos, context.getPlayer());
                }
                return true;
            }
            if(!offsetAdded){
                for (IUseAction useAction : USE_ACTIONS) {
	                if (useAction.attempItemUseTarget(context.getItemInHand(), context.getLevel(), pos, blockCable)) {
	                    return true;
	                }
            	}
            }
        }
        return false;
    }

    protected void afterItemUse(ItemUseContext context, BlockPos pos, BlockCable blockCable, boolean calledSuper) {
        if(!calledSuper) {
            playPlaceSound(context.getLevel(), pos);
            context.getItemInHand().shrink(1);
        }
        blockCable.setDisableCollisionBox(false);
    }

    @SuppressWarnings("deprecation")
    public static void playPlaceSound(World world, BlockPos pos) {
        Block block = RegistryEntries.BLOCK_CABLE;
        SoundType soundType = block.getSoundType(block.defaultBlockState());
        world.playLocalSound((double) ((float) pos.getX() + 0.5F), (double) ((float) pos.getY() + 0.5F), (double) ((float) pos.getZ() + 0.5F),
                soundType.getPlaceSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F, false);
    }

    public static void playBreakSound(World world, BlockPos pos, BlockState blockState) {
        world.globalLevelEvent(2001, pos, Block.getId(blockState));
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        ItemStack itemStack = context.getItemInHand();
        // Skips server-side entity collision detection for placing cables.
        // We temporary disable the collision box of the cable so that it can be placed even if an entity is in the way.
        BlockCable blockCable = (BlockCable) getBlock();
        blockCable.setDisableCollisionBox(true);

        // Avoid regular block placement when the target is an unreal cable.
        if(attempItemUseTarget(context, context.getClickedPos(), context.getClickedFace(), blockCable, false)) {
            afterItemUse(context, context.getClickedPos(), blockCable, false);
            return ActionResultType.SUCCESS;
        }

        // Change pos and side when we are targeting a block that is blocked by an unreal cable, so we want to target
        // the unreal cable.
        BlockPos posOffset = context.getClickedPos().relative(context.getClickedFace());
        if(attempItemUseTarget(context, posOffset, context.getClickedFace().getOpposite(), blockCable, true)) {
            afterItemUse(context, posOffset, blockCable, false);
            return ActionResultType.SUCCESS;
        }

        ActionResultType ret = super.useOn(context);
        afterItemUse(context, context.getClickedPos(), blockCable, true);
        return ret;
    }

    public static interface IUseAction {

        /**
         * Attempt to use the given item.
         * @param itemStack The item stack that is being used.
         * @param world The world.
         * @param pos The position.
         * @param blockCable The cable block instance.
         * @return If the use action was applied.
         */
        public boolean attempItemUseTarget(ItemStack itemStack, World world, BlockPos pos, BlockCable blockCable);

        /**
         * If the block can be placed at the given position.
         * @param world The world.
         * @param pos The position.
         * @return If the block can be placed.
         */
        public boolean canPlaceAt(World world, BlockPos pos);

    }

}
