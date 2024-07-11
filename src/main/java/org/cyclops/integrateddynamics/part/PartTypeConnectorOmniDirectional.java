package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.path.ISidedPathElement;
import org.cyclops.integrateddynamics.capability.path.SidedPathElement;
import org.cyclops.integrateddynamics.core.block.IgnoredBlock;
import org.cyclops.integrateddynamics.core.block.IgnoredBlockStatus;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.part.PartTypes;

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

    public PartTypeConnectorOmniDirectional(String name) {
        super(name, new PartRenderPosition(0.25F, 0.3125F, 0.625F, 0.625F));
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
    public ItemStack getItemStack(ValueDeseralizationContext valueDeseralizationContext, State state, boolean saveState) {
        ItemStack itemStack = super.getItemStack(valueDeseralizationContext, state, saveState);
        if (state.hasConnectorId()) {
            itemStack.set(RegistryEntries.DATACOMPONENT_OMNIDIRECTIONAL_GROUP, state.getGroupId());
        }
        return itemStack;
    }

    @Override
    public State getState(ValueDeseralizationContext valueDeseralizationContext, ItemStack itemStack) {
        State state = super.getState(valueDeseralizationContext, itemStack);
        if (itemStack.has(RegistryEntries.DATACOMPONENT_OMNIDIRECTIONAL_GROUP)) {
            state.setGroupId(itemStack.get(RegistryEntries.DATACOMPONENT_OMNIDIRECTIONAL_GROUP));
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
    public void loadTooltip(State state, List<Component> lines) {
        super.loadTooltip(state, lines);
        lines.add(Component.translatable(L10NValues.PART_TOOLTIP_MONODIRECTIONALCONNECTOR_GROUP, state.getGroupId()));
    }

    @Override
    public void loadTooltip(ItemStack itemStack, List<Component> lines) {
        super.loadTooltip(itemStack, lines);
        if (itemStack.has(RegistryEntries.DATACOMPONENT_OMNIDIRECTIONAL_GROUP)) {
            lines.add(Component.translatable(L10NValues.PART_TOOLTIP_MONODIRECTIONALCONNECTOR_GROUP,
                    itemStack.get(RegistryEntries.DATACOMPONENT_OMNIDIRECTIONAL_GROUP)));
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
                .setValue(IgnoredBlock.FACING, side)
                .setValue(IgnoredBlockStatus.STATUS, status);
    }

    public static ItemStack transformCraftingOutput(CraftingInput inventory, ItemStack staticOutput) {
        // When crafting the item, either copy the group id from the existing item or generate a new id.
        int groupId = -1, stackCount = 0;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack slotStack = inventory.getItem(i);
            if (!slotStack.isEmpty()) {
                ++stackCount;
                if(groupId == -1 && slotStack.getItem() == PartTypes.CONNECTOR_OMNI.getItem() && slotStack.has(RegistryEntries.DATACOMPONENT_OMNIDIRECTIONAL_GROUP)) {
                    groupId = slotStack.get(RegistryEntries.DATACOMPONENT_OMNIDIRECTIONAL_GROUP);
                }
            }
        }
        if(stackCount == 1) {
            groupId = -1; // If we're resetting a connector, give it a new ID
        }

        if (groupId < 0) {
            groupId = generateGroupId();
        }

        staticOutput = staticOutput.copy();
        staticOutput.set(RegistryEntries.DATACOMPONENT_OMNIDIRECTIONAL_GROUP, groupId);

        return staticOutput;
    }

    @Override
    public InteractionResult onPartActivated(State partState, BlockPos pos, Level world, Player player, InteractionHand hand, ItemStack heldItem, BlockHitResult hit) {
        // Drop through if the player is sneaking
        if(player.isSecondaryUseActive() || !partState.isEnabled()) {
            return InteractionResult.PASS;
        }
        if (world.isClientSide()) {
            player.displayClientMessage(Component.translatable(L10NValues.PART_TOOLTIP_MONODIRECTIONALCONNECTOR_GROUP,
                    partState.getGroupId()), true);
        }

        return InteractionResult.SUCCESS;
    }

    public static class State extends PartTypeConnector.State<PartTypeConnectorOmniDirectional> {

        private int groupId = -1;
        private boolean addedToGroup = false;

        @Override
        public void writeToNBT(ValueDeseralizationContext valueDeseralizationContext, CompoundTag tag) {
            super.writeToNBT(valueDeseralizationContext, tag);
            tag.putInt("groupId", groupId);
        }

        @Override
        public void readFromNBT(ValueDeseralizationContext valueDeseralizationContext, CompoundTag tag) {
            super.readFromNBT(valueDeseralizationContext, tag);
            this.groupId = tag.getInt("groupId");
        }

        @Override
        public Set<ISidedPathElement> getReachableElements() {
            if (hasConnectorId()) {
                Set<ISidedPathElement> pathElements = Sets.newTreeSet();
                for (PartPos pos : PartTypeConnectorOmniDirectional.LOADED_GROUPS.getPositions(getGroupId())) {
                    if (!pos.equals(this.getPartPos())) {
                        BlockEntityHelpers.getCapability(pos.getPos(), pos.getSide(), Capabilities.PathElement.BLOCK)
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

        public void onStartedEvent(ServerStartedEvent event) {
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
                    NetworkHelpers.initNetwork(position.getPos().getLevel(true), position.getPos().getBlockPos(), position.getSide());
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
                    NetworkHelpers.initNetwork(pos.getPos().getLevel(true), pos.getPos().getBlockPos(), pos.getSide());
                }
                modifyingPositions = false;
            }
        }

        public boolean isModifyingPositions() {
            return modifyingPositions;
        }
    }
}
