package org.cyclops.integrateddynamics.api.part;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.init.IInitListener;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkEventListener;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetworkElement;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * A type of part that can be inserted into a {@link IPartContainer}.
 * Only one unique instance for each part should exist, the values are stored inside an
 * {@link IPartState}.
 * @param <P> The part type.
 * @param <S> The part state type.
 * @author rubensworks
 */
public interface IPartType<P extends IPartType<P, S>, S extends IPartState<P>> extends INetworkEventListener<IPartNetworkElement<P, S>> {

    /**
     * @return The unique name for this part type.
     */
    public String getName();

    /**
     * @return The unlocalized base name of this part. (Without the .name suffix)
     */
    public String getTranslationKeyBase();

    /**
     * @return The unlocalized name of this part. (With the .name suffix)
     */
    public String getTranslationKey();

    /**
     * @return JSON model path for the block representation of this part.
     */
    public ResourceLocation getBlockModelPath();

    /**
     * @return The item associated with this part type.
     */
    public Item getItem();

    /**
     * @param state The state
     * @return If this element is solid.
     */
    public boolean isSolid(S state);

    /**
     * @return The position part occupies, used to calculate the required render lengths.
     *         This part is assumed to be aligned at the edge of the block for the depth, while centered on width and height.
     */
    public PartRenderPosition getPartRenderPosition();

    /**
     * Called on the Integrated Dynamics mod initialization steps.
     * @param initStep The init step.
     */
    public void onInit(IInitListener.Step initStep);

    /**
     * Write the properties of this part to NBT.
     * An identificator for this part is not required, this is written somewhere else.
     * @param tag The tag to write to. This tag is guaranteed to be empty.
     * @param partState The state of this part.
     */
    public void toNBT(NBTTagCompound tag, S partState);

    /**
     * Read the properties of this part from nbt.
     * This tag is guaranteed to only contain data for this part.
     * @param tag The tag to read from.
     * @return The state of this part.
     */
    public S fromNBT(NBTTagCompound tag);

    /**
     * @return The default state of this part.
     */
    public S getDefaultState();

    /**
     * Set the update interval for this part.
     * @param state The state
     * @param updateInterval The tick interval to update this element.
     */
    public void setUpdateInterval(S state, int updateInterval);

    /**
     * @param state The state
     * @return The tick interval to update this element.
     */
    public int getUpdateInterval(S state);

    /**
     * @param state The state
     * @return The minimum allowed tick interval to update this element.
     */
    public int getMinimumUpdateInterval(S state);

    /**
     * Set the priority and channel of this part in the network.
     * @deprecated Should only be called from {@link INetwork#setPriorityAndChannel(INetworkElement, int, int)}!
     * @param network The network to update in.
     * @param partNetwork The part network to update in.
     * @param target The target block.
     * @param state The state
     * @param priority The new priority
     * @param channel The new channel
     */
    @Deprecated
    public void setPriorityAndChannel(INetwork network, IPartNetwork partNetwork, PartTarget target, S state, int priority, int channel);

    /**
     * @param state The state
     * @return The priority of this part in the network.
     */
    public int getPriority(S state);

    /**
     * @param state The state
     * @return The channel of this part in the network.
     */
    public int getChannel(S state);

    /**
     * Indicate that the given part should interact with the given side of the target.
     * @param state The state
     * @param side The side of the target block to interact with.
     *             Null removes the side override.
     */
    public void setTargetSideOverride(S state, @Nullable EnumFacing side);

    /**
     * @param state The state
     * @return The overridden side of the target block to interact with. Can be null.
     */
    @Nullable
    public EnumFacing getTargetSideOverride(S state);

    /**
     * Get the part target for this part.
     * @param pos The center position of this part.
     * @param state The state.
     * @return The part target.
     */
    public PartTarget getTarget(PartPos pos, S state);

    /**
     * @param state The state
     * @return If this element should be updated. This method is only called once during network initialization.
     */
    public boolean isUpdate(S state);

    /**
     * Update at the tick interval specified.
     * @param network The network to update in.
     * @param partNetwork The part network to update in.
     * @param target The target block.
     * @param state The state
     */
    public void update(INetwork network, IPartNetwork partNetwork, PartTarget target, S state);

    /**
     * Called right before the network is terminated or will be reset.
     * @param network The network to update in.
     * @param partNetwork The part network to update in.
     * @param target The target block.
     * @param state The state
     */
    public void beforeNetworkKill(INetwork network, IPartNetwork partNetwork, PartTarget target, S state);

    /**
     * Called right after this network is initialized.
     * @param network The network to update in.
     * @param partNetwork The part network to update in.
     * @param target The target block.
     * @param state The state
     */
    public void afterNetworkAlive(INetwork network, IPartNetwork partNetwork, PartTarget target, S state);

    /**
     * Called right after this network has come alive again,
     * for example after a network restart.
     * @param network The network to update in.
     * @param partNetwork The part network to update in.
     * @param target The target block.
     * @param state The state
     */
    public void afterNetworkReAlive(INetwork network, IPartNetwork partNetwork, PartTarget target, S state);

    /**
     * Get the itemstack from the given state.
     * @param state The state
     * @param saveState If the part state should be saved in the item.
     * @return The itemstack possibly containing the state information.
     */
    public ItemStack getItemStack(S state, boolean saveState);

    /**
     * Get the itemstack from the given state.
     * @param world The world.
     * @param pos The position.
     * @param state The state.
     * @return The itemstack possibly containing the state information.
     */
    public ItemStack getPickBlock(World world, BlockPos pos, S state);

    /**
     * Get the part state from the given itemstack.
     * @param itemStack The itemstack possibly containing state information.
     * @return The state contained in the itemstack or the default part state.
     */
    public S getState(ItemStack itemStack);

    /**
     * Add the itemstacks to drop when this element is removed.
     * @param target The target.
     * @param state The state
     * @param itemStacks The itemstack list to add to.
     * @param dropMainElement If the part itself should also be dropped.
     * @param saveState If the part state should be saved in the item.
     */
    public void addDrops(PartTarget target, S state, List<ItemStack> itemStacks, boolean dropMainElement, boolean saveState);

    /**
     * Called when this element is added to the network.
     * @param network The network to update in.
     * @param partNetwork The part network to update in.
     * @param target The target block.
     * @param state The state
     */
    public void onNetworkAddition(INetwork network, IPartNetwork partNetwork, PartTarget target, S state);

    /**
     * Called when this element is removed from the network.
     * @param network The network to update in.
     * @param partNetwork The part network to update in.
     * @param target The target block.
     * @param state The state
     */
    public void onNetworkRemoval(INetwork network, IPartNetwork partNetwork, PartTarget target, S state);

    /**
     * Create a network element for this part type.
     * @param partContainer The container this part is/will be part of.
     * @param pos The position this network element is/will be placed at.
     * @param side The side this network element is/will be placed at.
     * @return A new network element instance.
     */
    public INetworkElement createNetworkElement(IPartContainer partContainer, DimPos pos, EnumFacing side);

    /**
     * Called when a part is right-clicked.
     * @param world The world.
     * @param pos The position of the block this part is part of.
     * @param partState The state of this part.
     * @param player The player activating the part.
     * @param hand The hand in use by the player.
     * @param heldItem The held item.
     * @param side The side this part is attached on.
     * @param hitX The X hit position.
     * @param hitY The Y hit position.
     * @param hitZ The Z hit position.
     * @return True if the further processing should be stopped.
     */
    public boolean onPartActivated(World world, BlockPos pos, S partState, EntityPlayer player, EnumHand hand,
                                   ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ);

    /**
     * Get the base block state that will be rendered for this part.
     * An appropriate {@link org.cyclops.integrateddynamics.core.block.IgnoredBlock#FACING} property will be set.
     * @param partContainer The part entity.
     * @param side The position of the part.
     * @return The block state to render with.
     */
    public IBlockState getBlockState(IPartContainer partContainer, EnumFacing side);

    /**
     * @return The default block state representation of this part.
     */
    public BlockStateContainer getBaseBlockState();

    /**
     * Called when a block update occurs
     * @param world The world.
     * @param pos The position.
     * @param partState The part state.
     * @param random A random instance.
     */
    public void updateTick(World world, BlockPos pos, S partState, Random random);

    /**
     * Called when this element is about to be removed.
     * @param network The network.
     * @param partNetwork The part network to update in.
     * @param target The target block.
     * @param state The state
     */
    public void onPreRemoved(INetwork network, IPartNetwork partNetwork, PartTarget target, S state);

    /**
     * Called after this element has been removed.
     * @param network The network.
     * @param partNetwork The part network to update in.
     * @param target The target block.
     * @param state The state
     */
    public void onPostRemoved(INetwork network, IPartNetwork partNetwork, PartTarget target, S state);

    /**
     * Called when a neighbouring block is updated, more specifically when
     * {@link net.minecraft.block.Block#onNeighborChange(IBlockAccess, BlockPos, BlockPos)} or
     * {@link Block#onNeighborChange(IBlockAccess, BlockPos, BlockPos)} is called.
     * @param network The network to update in.
     * @param partNetwork The part network to update in.
     * @param target The target block.
     * @param state The state
     * @param world The world in which the neighbour was updated.
     * @param neighborBlock The block type of the neighbour that was updated.
     */
    public void onBlockNeighborChange(@Nullable INetwork network, @Nullable IPartNetwork partNetwork, PartTarget target, S state, IBlockAccess world, Block neighborBlock);

    /**
     * @param state The state
     * @return The consumption rate of this part for the given state.
     */
    public int getConsumptionRate(S state);

    /**
     * Called after the element was updated or not.
     * If the update was not called, this can be because the network did not contain
     * enough energy to let this element work.
     * @param network The network to update in.
     * @param partNetwork The part network to update in.
     * @param target The target block.
     * @param state The state
     * @param updated If the {@link INetworkElement#update(INetwork)} was called.
     */
    public void postUpdate(INetwork network, IPartNetwork partNetwork, PartTarget target, S state, boolean updated);

    /**
     * @param state The state
     * @return If this part is enabled.
     */
    public boolean isEnabled(S state);

    /**
     * Set if this part should work.
     * @param state The state
     * @param enabled If it should work.
     */
    public void setEnabled(S state, boolean enabled);

    /**
     * Add tooltip lines for this aspect when this part is being hovered by a mod like WAILA.
     * @param state The state.
     * @param lines The list to add lines to.
     */
    public void loadTooltip(S state, List<String> lines);

    /**
     * Add tooltip lines for this aspect when this part's item is being hovered.
     * @param itemStack The itemstack.
     * @param lines The list to add lines to.
     */
    public void loadTooltip(ItemStack itemStack, List<String> lines);

    /**
     * Check if the given state change should trigger a block render update.
     * This is only called client-side.
     * The new and old partstates are never both null, at most one will be null.
     * @param oldPartState The old part state.
     * @param newPartState The new part state.
     * @return If it should trigger a block render update.
     */
    public boolean shouldTriggerBlockRenderUpdate(@Nullable S oldPartState, @Nullable S newPartState);

    /**
     * @param state The state.
     * @return If this part should force the block to be transparent to light.
     */
    public boolean forceLightTransparency(S state);

}
