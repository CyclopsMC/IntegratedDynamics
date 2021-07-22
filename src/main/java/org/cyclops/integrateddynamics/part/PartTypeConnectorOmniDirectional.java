package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.path.ISidedPathElement;
import org.cyclops.integrateddynamics.capability.path.PathElementConfig;
import org.cyclops.integrateddynamics.capability.path.SidedPathElement;
import org.cyclops.integrateddynamics.core.block.IgnoredBlock;
import org.cyclops.integrateddynamics.core.block.IgnoredBlockStatus;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * An omnidirectional wireless connector part that can connect to
 * all other monodirectional connectors of the same group anywhere in any dimension.
 * @author rubensworks
 */
public class PartTypeConnectorOmniDirectional extends PartTypeConnector<PartTypeConnectorOmniDirectional, PartTypeConnectorOmniDirectional.State> {

    public static LoadedGroups LOADED_GROUPS = new LoadedGroups();
    private static String NBT_KEY_ID = "omnidir-group-key";

    public PartTypeConnectorOmniDirectional(String name) {
        super(name, new PartRenderPosition(0.25F, 0.3125F, 0.625F, 0.625F));
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public int getConsumptionRate(State state) {
        return GeneralConfig.connectorOmniDirectionalBaseConsumption;
    }

    @Override
    public PartTypeConnectorOmniDirectional.State constructDefaultState() {
        return new PartTypeConnectorOmniDirectional.State();
    }

    @Override
    public ItemStack getItemStack(State state, boolean saveState) {
        ItemStack itemStack = super.getItemStack(state, saveState);
        if (state.hasConnectorId()) {
            CompoundNBT tag = itemStack.getOrCreateTag();
            tag.putInt(NBT_KEY_ID, state.getGroupId());
        }
        return itemStack;
    }

    @Override
    public State getState(ItemStack itemStack) {
        State state = super.getState(itemStack);
        CompoundNBT tag = itemStack.getTag();
        if (tag != null && tag.contains(NBT_KEY_ID, Constants.NBT.TAG_INT)) {
            state.setGroupId(tag.getInt(NBT_KEY_ID));
        } else {
            state.setGroupId(PartTypeConnectorOmniDirectional.generateGroupId());
        }
        return state;
    }

    @Override
    public void onNetworkAddition(INetwork network, IPartNetwork partNetwork, PartTarget target, State state) {
        super.onNetworkAddition(network, partNetwork, target, state);
        addPosition(network, state, target.getCenter());
    }

    @Override
    public void onPostRemoved(INetwork network, IPartNetwork partNetwork, PartTarget target, State state) {
        super.onPostRemoved(network, partNetwork, target, state);
        removePosition(network, state, target.getCenter());
    }

    protected void addPosition(INetwork network, State state, PartPos pos) {
        if (!PartTypeConnectorOmniDirectional.LOADED_GROUPS.isModifyingPositions()
                && !state.isAddedToGroup()) {
            state.setAddedToGroup(true);
            PartTypeConnectorOmniDirectional.LOADED_GROUPS.addPosition(state.getGroupId(), pos, network.isInitialized());
        }
    }

    protected void removePosition(INetwork network, State state, PartPos pos) {
        if (!PartTypeConnectorOmniDirectional.LOADED_GROUPS.isModifyingPositions()
                && state.isAddedToGroup()) {
            if (state.hasConnectorId()) {
                state.setAddedToGroup(false);
                PartTypeConnectorOmniDirectional.LOADED_GROUPS.removePosition(state.getGroupId(), pos, network.isInitialized());
            }
        }
    }

    public static int generateGroupId() {
        return IntegratedDynamics.globalCounters.getNext("omnidir-connectors");
    }

    @Override
    public void loadTooltip(State state, List<ITextComponent> lines) {
        super.loadTooltip(state, lines);
        lines.add(new TranslationTextComponent(L10NValues.PART_TOOLTIP_MONODIRECTIONALCONNECTOR_GROUP, state.getGroupId()));
    }

    @Override
    public void loadTooltip(ItemStack itemStack, List<ITextComponent> lines) {
        super.loadTooltip(itemStack, lines);
        if (itemStack.hasTag()) {
            lines.add(new TranslationTextComponent(L10NValues.PART_TOOLTIP_MONODIRECTIONALCONNECTOR_GROUP,
                    itemStack.getTag().getInt(NBT_KEY_ID)));
        }
    }

    protected IgnoredBlockStatus.Status getStatus(PartTypeConnectorOmniDirectional.State state) {
        return state != null && state.hasConnectorId()
                ? IgnoredBlockStatus.Status.ACTIVE : IgnoredBlockStatus.Status.INACTIVE;
    }

    @Override
    public BlockState getBlockState(IPartContainer partContainer, Direction side) {
        IgnoredBlockStatus.Status status = getStatus(partContainer != null
                ? (PartTypeConnectorOmniDirectional.State) partContainer.getPartState(side) : null);
        return super.getBlockState(partContainer, side)
                .with(IgnoredBlock.FACING, side)
                .with(IgnoredBlockStatus.STATUS, status);
    }

    @SubscribeEvent
    public void onCrafted(PlayerEvent.ItemCraftedEvent event) {
        // When crafting the item, either copy the group id from the existing item or generate a new id.
        if (event.getCrafting().getItem() == this.getItem()) {
            int groupId = -1, stackCount = 0;
            for (int i = 0; i < event.getInventory().getSizeInventory(); i++) {
                ItemStack slotStack = event.getInventory().getStackInSlot(i);
                if (!slotStack.isEmpty()) {
                    ++stackCount;
                    if(groupId == -1 && slotStack.getItem() == this.getItem() && slotStack.hasTag()) {
                        CompoundNBT tag = slotStack.getTag();
                        if (tag.contains(NBT_KEY_ID, Constants.NBT.TAG_INT)) {
                            groupId = tag.getInt(NBT_KEY_ID);
                        }
                    }
                }
            }
            if(stackCount == 1) {
                groupId = -1; // If we're resetting a connector, give it a new ID
            }

            if (groupId < 0) {
                groupId = event.getPlayer().getEntityWorld().isRemote() ? -1 : generateGroupId();
            }
            CompoundNBT tag = event.getCrafting().getOrCreateTag();
            tag.putInt(NBT_KEY_ID, groupId);
        }
    }

    @Override
    public ActionResultType onPartActivated(State partState, BlockPos pos, World world, PlayerEntity player, Hand hand, ItemStack heldItem, BlockRayTraceResult hit) {
        // Drop through if the player is sneaking
        if(player.isSecondaryUseActive() || !partState.isEnabled()) {
            return ActionResultType.PASS;
        }
        if (world.isRemote()) {
            player.sendStatusMessage(new TranslationTextComponent(L10NValues.PART_TOOLTIP_MONODIRECTIONALCONNECTOR_GROUP,
                    partState.getGroupId()), true);
        }

        return ActionResultType.SUCCESS;
    }

    public static class State extends PartTypeConnector.State<PartTypeConnectorOmniDirectional> {

        private int groupId = -1;
        private boolean addedToGroup = false;

        @Override
        public void writeToNBT(CompoundNBT tag) {
            super.writeToNBT(tag);
            tag.putInt(NBT_KEY_ID, groupId);
        }

        @Override
        public void readFromNBT(CompoundNBT tag) {
            super.readFromNBT(tag);
            this.groupId = tag.getInt(NBT_KEY_ID);
        }

        @Override
        public Set<ISidedPathElement> getReachableElements() {
            if (hasConnectorId()) {
                Set<ISidedPathElement> pathElements = Sets.newTreeSet();
                for (PartPos pos : PartTypeConnectorOmniDirectional.LOADED_GROUPS.getPositions(getGroupId())) {
                    if (!pos.equals(this.getPartPos())) {
                        TileHelpers.getCapability(pos.getPos(), pos.getSide(), PathElementConfig.CAPABILITY)
                                .ifPresent(pathElement -> pathElements.add(SidedPathElement.of(pathElement, pos.getSide())));
                    }
                }
                return pathElements;
            }
            return Collections.emptySet();
        }

        public int getGroupId() {
            return groupId;
        }

        public void setGroupId(int groupId) {
            this.groupId = groupId;
            sendUpdate();
        }

        public boolean hasConnectorId() {
            return this.groupId >= 0;
        }

        public boolean isAddedToGroup() {
            return addedToGroup;
        }

        public void setAddedToGroup(boolean addedToGroup) {
            this.addedToGroup = addedToGroup;
        }
    }

    public static class LoadedGroups {

        private Int2ObjectMap<Set<PartPos>> groupPositions = new Int2ObjectOpenHashMap<>();
        private boolean modifyingPositions = false;

        public void onStartedEvent(FMLServerStartedEvent event) {
            // Reset to avoid ghost-groups on world-change.
            groupPositions.clear();
        }

        public Set<PartPos> getPositions(int group) {
            Set<PartPos> positions = groupPositions.get(group);
            return positions != null ? positions : Collections.<PartPos>emptySet();
        }

        protected void initNetworkGroup(Set<PartPos> positions) {
            for (PartPos position : positions) {
                if (position.getPos().isLoaded()) {
                    NetworkHelpers.initNetwork(position.getPos().getWorld(true), position.getPos().getBlockPos(), position.getSide());
                }
            }
        }

        public void addPosition(int group, PartPos pos, boolean initNetwork) {
            Set<PartPos> positions = groupPositions.get(group);
            if (positions == null) {
                groupPositions.put(group, positions = Sets.newTreeSet());
            }
            positions.add(pos);

            if (initNetwork) {
                modifyingPositions = true;
                initNetworkGroup(positions);
                modifyingPositions = false;
            }
        }

        public void removePosition(int group, PartPos pos, boolean initNetwork) {
            Set<PartPos> positions = groupPositions.get(group);
            if (positions == null) {
                groupPositions.put(group, positions = Sets.newTreeSet());
            }
            positions.remove(pos);

            if (initNetwork) {
                modifyingPositions = true;
                initNetworkGroup(positions);
                if (pos.getPos().isLoaded()) {
                    NetworkHelpers.initNetwork(pos.getPos().getWorld(true), pos.getPos().getBlockPos(), pos.getSide());
                }
                modifyingPositions = false;
            }
        }

        public boolean isModifyingPositions() {
            return modifyingPositions;
        }
    }
}
