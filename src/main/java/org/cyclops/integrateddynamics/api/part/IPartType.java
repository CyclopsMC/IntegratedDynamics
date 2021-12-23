package org.cyclops.integrateddynamics.api.part;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkEventListener;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetworkElement;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
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
    public ResourceLocation getUniqueName();

    /**
     * @return The unlocalized name of this part.
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
     * Write the properties of this part to NBT.
     * An identificator for this part is not required, this is written somewhere else.
     * @param tag The tag to write to. This tag is guaranteed to be empty.
     * @param partState The state of this part.
     */
    public void toNBT(CompoundNBT tag, S partState);

    /**
     * Read the properties of this part from nbt.
     * This tag is guaranteed to only contain data for this part.
     * @param tag The tag to read from.
     * @return The state of this part.
     */
    public S fromNBT(CompoundNBT tag);

    /**
     * @return The default state of this part.
     */
    public S defaultBlockState();

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
    public void setTargetSideOverride(S state, @Nullable Direction side);

    /**
     * @param state The state
     * @return The overridden side of the target block to interact with. Can be null.
     */
    @Nullable
    public Direction getTargetSideOverride(S state);

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
    public INetworkElement createNetworkElement(IPartContainer partContainer, DimPos pos, Direction side);

    /**
     * Called when a part is right-clicked.
     * @param partState The state of this part.
     * @param pos The position of the block this part is part of.
     * @param world The world.
     * @param player The player activating the part.
     * @param hand The hand in use by the player.
     * @param heldItem The held item.
     * @param hit The ray trace hit result.
     * @return The action result.
     */
    public ActionResultType onPartActivated(S partState, BlockPos pos, World world, PlayerEntity player, Hand hand,
                                            ItemStack heldItem, BlockRayTraceResult hit);

    /**
     * Get the base block state that will be rendered for this part.
     * An appropriate {@link org.cyclops.integrateddynamics.core.block.IgnoredBlock#FACING} property will be set.
     * @param partContainer The part entity.
     * @param side The position of the part.
     * @return The block state to render with.
     */
    public BlockState getBlockState(IPartContainer partContainer, Direction side);

    /**
     * @return The default block state representation of this part.
     */
    public BlockState getBaseBlockState();

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
     * {@link net.minecraft.block.Block#neighborChanged(BlockState, World, BlockPos, Block, BlockPos, boolean)} or
     * {@link Block#onNeighborChange(BlockState, IWorldReader, BlockPos, BlockPos)} is called.
     * @param network The network to update in.
     * @param partNetwork The part network to update in.
     * @param target The target block.
     * @param state The state
     * @param world The world in which the neighbour was updated.
     * @param neighbourBlock The block type of the neighbour that was updated.
     * @param neighbourBlockPos The position of the neighbour that was updated.
     */
    public void onBlockNeighborChange(@Nullable INetwork network, @Nullable IPartNetwork partNetwork, PartTarget target,
                                      S state, IBlockReader world, Block neighbourBlock, BlockPos neighbourBlockPos);

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
    public void loadTooltip(S state, List<ITextComponent> lines);

    /**
     * Add tooltip lines for this aspect when this part's item is being hovered.
     * @param itemStack The itemstack.
     * @param lines The list to add lines to.
     */
    public void loadTooltip(ItemStack itemStack, List<ITextComponent> lines);

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

    /**
     * {@link #writeExtraGuiData(PacketBuffer, PartPos, ServerPlayerEntity)}.
     * @return The optional container provider for the part type gui.
     * @param pos The part position. May be null when called client-side, for checking presence.
     */
    public default Optional<INamedContainerProvider> getContainerProvider(PartPos pos) {
        return Optional.empty();
    };

    /**
     * This method can be overridden for cases when additional data needs to be sent to clients when opening containers.
     * @param packetBuffer A packet buffer that can be written to.
     * @param pos A part position.
     * @param player The player opening the gui.
     */
    public default void writeExtraGuiData(PacketBuffer packetBuffer, PartPos pos, ServerPlayerEntity player) {

    }

    /**
     * {@link #writeExtraGuiDataSettings(PacketBuffer, PartPos, ServerPlayerEntity)}.
     * @return The optional container provider for the part settings gui.
     * @param pos The part position. May be null when called client-side, for checking presence.
     */
    public default Optional<INamedContainerProvider> getContainerProviderSettings(PartPos pos) {
        return Optional.empty();
    };

    /**
     * This method can be overridden for cases when additional data needs to be sent to clients
     * when opening settingscontainers.
     * @param packetBuffer A packet buffer that can be written to.
     * @param pos A part position.
     * @param player The player opening the settings gui.
     */
    public default void writeExtraGuiDataSettings(PacketBuffer packetBuffer, PartPos pos, ServerPlayerEntity player) {

    }

}
