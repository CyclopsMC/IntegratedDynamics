package org.cyclops.integrateddynamics.api.part;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.init.IInitListener;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetworkElement;
import org.cyclops.integrateddynamics.api.network.event.INetworkEvent;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Default implementation of {@link IPartType}.
 * @author rubensworks
 */
public abstract class PartTypeAdapter<P extends IPartType<P, S>, S extends IPartState<P>> implements IPartType<P, S> {

    private String translationKey = null;

    @Override
    public String getTranslationKey() {
        return translationKey != null ? translationKey : (translationKey = getTranslationKeyBase() + ".name");
    }

    @Override
    public boolean isSolid(S state) {
        return false;
    }

    @Override
    public void onInit(IInitListener.Step initStep) {

    }

    @Override
    public void toNBT(NBTTagCompound tag, S partState) {
        partState.writeToNBT(tag);
    }

    @Override
    public S fromNBT(NBTTagCompound tag) {
        S partState = constructDefaultState();
        partState.readFromNBT(tag);
        partState.gatherCapabilities((P) this);
        return partState;
    }

    @Override
    public void setUpdateInterval(S state, int updateInterval) {
        state.setUpdateInterval(updateInterval);
    }

    @Override
    public int getUpdateInterval(S state) {
        return state.getUpdateInterval();
    }

    @Override
    public int getMinimumUpdateInterval(S state) {
        return 1;
    }

    @Override
    public void setPriorityAndChannel(INetwork network, IPartNetwork partNetwork, PartTarget target, S state, int priority, int channel) {
        //noinspection deprecation
        state.setPriority(priority);
        state.setChannel(channel);
    }

    @Override
    public int getPriority(S state) {
        return state.getPriority();
    }

    @Override
    public int getChannel(S state) {
        return state.getChannel();
    }

    @Override
    public void setTargetSideOverride(S state, @Nullable EnumFacing side) {
        state.setTargetSideOverride(side);
    }

    @Nullable
    @Override
    public EnumFacing getTargetSideOverride(S state) {
        return state.getTargetSideOverride();
    }

    @Override
    public PartTarget getTarget(PartPos pos, S state) {
        PartTarget target = PartTarget.fromCenter(pos);
        EnumFacing sideOverride = getTargetSideOverride(state);
        if (sideOverride != null) {
            target = target.forTargetSide(sideOverride);
        }
        return target;
    }

    @Override
    public boolean isUpdate(S state) {
        return false;
    }

    @Override
    public void update(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {

    }

    @Override
    public void beforeNetworkKill(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {

    }

    @Override
    public void afterNetworkAlive(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {

    }

    @Override
    public void afterNetworkReAlive(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {

    }

    @Override
    public ItemStack getItemStack(S state, boolean saveState) {
        ItemStack itemStack = new ItemStack(getItem());
        if (saveState) {
            NBTTagCompound tag = new NBTTagCompound();
            toNBT(tag, state);
            itemStack.setTagCompound(tag);
        }
        return itemStack;
    }

    @Override
    public ItemStack getPickBlock(World world, BlockPos pos, S state) {
        return getItemStack(state, false);
    }

    @Override
    public S getState(ItemStack itemStack) {
        S partState = null;
        if(!itemStack.isEmpty() && itemStack.getTagCompound() != null
                && itemStack.getTagCompound().hasKey("id", MinecraftHelpers.NBTTag_Types.NBTTagInt.ordinal())) {
            partState = fromNBT(itemStack.getTagCompound());
        }
        if(partState == null) {
            partState = getDefaultState();
        }
        return partState;
    }

    /**
     * @return Constructor call for a new default state for this part type.
     */
    protected abstract S constructDefaultState();

    @Override
    public S getDefaultState() {
        S defaultState = constructDefaultState();
        defaultState.generateId();
        defaultState.gatherCapabilities((P) this);
        return defaultState;
    }

    @Override
    public void addDrops(PartTarget target, S state, List<ItemStack> itemStacks, boolean dropMainElement, boolean saveState) {
        if(dropMainElement) {
            itemStacks.add(getItemStack(state, saveState));
        }
    }

    @Override
    public void onNetworkAddition(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {

    }

    @Override
    public void onNetworkRemoval(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {

    }

    @Override
    public boolean onPartActivated(World world, BlockPos pos, S partState, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        return false;
    }

    @Override
    public void updateTick(World world, BlockPos pos, S partState, Random random) {

    }

    @Override
    public void onPreRemoved(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {

    }

    @Override
    public void onPostRemoved(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {

    }

    @Override
    public void onBlockNeighborChange(INetwork network, IPartNetwork partNetwork, PartTarget target, S state, IBlockAccess world, Block neighborBlock) {

    }

    @Override
    public int getConsumptionRate(S state) {
        return 0;
    }

    @Override
    public void postUpdate(INetwork network, IPartNetwork partNetwork, PartTarget target, S state, boolean updated) {
        setEnabled(state, updated);
    }

    @Override
    public boolean isEnabled(S state) {
        return state.isEnabled();
    }

    @Override
    public void setEnabled(S state, boolean enabled) {
        state.setEnabled(enabled);
    }

    @Override
    public void loadTooltip(S state, List<String> lines) {

    }

    @Override
    public void loadTooltip(ItemStack itemStack, List<String> lines) {

    }

    @Override
    public boolean shouldTriggerBlockRenderUpdate(@Nullable S oldPartState, @Nullable S newPartState) {
        return oldPartState == null || newPartState == null || oldPartState.isForceBlockRenderUpdateAndReset();
    }

    @Override
    public boolean hasEventSubscriptions() {
        return false;
    }

    @Override
    public Set<Class<? extends INetworkEvent>> getSubscribedEvents() {
        return Collections.emptySet();
    }

    @Override
    public void onEvent(INetworkEvent event, IPartNetworkElement<P, S> networkElement) {

    }
}
