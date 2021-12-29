package org.cyclops.integrateddynamics.core.part.panel;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxy;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.network.event.INetworkEvent;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartTypeActiveVariable;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.block.IgnoredBlock;
import org.cyclops.integrateddynamics.core.block.IgnoredBlockStatus;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.helper.WrenchHelpers;
import org.cyclops.integrateddynamics.core.network.event.NetworkElementAddEvent;
import org.cyclops.integrateddynamics.core.network.event.VariableContentsUpdatedEvent;
import org.cyclops.integrateddynamics.core.part.PartStateActiveVariableBase;
import org.cyclops.integrateddynamics.core.part.PartTypeBase;
import org.cyclops.integrateddynamics.inventory.container.ContainerPartPanelVariableDriven;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A panel part that is driven by a contained variable.
 * @author rubensworks
 */
public abstract class PartTypePanelVariableDriven<P extends PartTypePanelVariableDriven<P, S>, S extends PartTypePanelVariableDriven.State<P, S>> extends PartTypePanel<P, S> implements IPartTypeActiveVariable<P, S> {

    public PartTypePanelVariableDriven(String name) {
        super(name);
    }

    @Override
    protected Block createBlock(BlockConfig blockConfig) {
        return new IgnoredBlockStatus();
    }

    @Override
    protected Map<Class<? extends INetworkEvent>, IEventAction> constructNetworkEventActions() {
        Map<Class<? extends INetworkEvent>, IEventAction> actions = super.constructNetworkEventActions();
        IEventAction<P, S, INetworkEvent> updateEventListener = (network, target, state, event) -> NetworkHelpers
                .getPartNetwork(network).ifPresent(partNetwork -> onVariableContentsUpdated(partNetwork, target, state));
        actions.put(VariableContentsUpdatedEvent.class, updateEventListener);
        actions.put(NetworkElementAddEvent.Post.class, updateEventListener);
        return actions;
    }

    @Override
    public void addDrops(PartTarget target, S state, List<ItemStack> itemStacks, boolean dropMainElement, boolean saveState) {
        for(int i = 0; i < state.getInventory().getContainerSize(); i++) {
            ItemStack itemStack = state.getInventory().getItem(i);
            if(!itemStack.isEmpty()) {
                itemStacks.add(itemStack);
            }
        }
        state.getInventory().clearContent();
        state.onVariableContentsUpdated((P) this, target);
        super.addDrops(target, state, itemStacks, dropMainElement, saveState);
    }

    @Override
    public void beforeNetworkKill(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {
        super.beforeNetworkKill(network, partNetwork, target, state);
        state.onVariableContentsUpdated((P) this, target);
    }

    @Override
    public void afterNetworkAlive(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {
        super.afterNetworkAlive(network, partNetwork, target, state);
        state.onVariableContentsUpdated((P) this, target);
    }

    @Override
    public boolean isUpdate(S state) {
        return true;
    }

    @Override
    public void update(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {
        super.update(network, partNetwork, target, state);
        IValue lastValue = state.getDisplayValue();
        IValue newValue = null;
        if(state.hasVariable()) {
            try {
                IVariable variable = state.getVariable(network, partNetwork);
                if(variable != null) {
                    newValue = variable.getValue();

                }
            } catch (EvaluationException e) {
                state.addGlobalError(e.getErrorMessage());
            }
        }
        if(!ValueHelpers.areValuesEqual(lastValue, newValue)) {
            onValueChanged(network, partNetwork, target, state, lastValue, newValue);

            // We can't call state.sendUpdate() here, so we must trigger a block update manually.
            // This was the cause of issue #46 which made it so that values that change after one tick are
            // NOT sent to the client.
            // This was because in each server tick, all tiles are first updated and then the networks.
            // Since sendUpdate marks a flag for the part to update, this caused a loss of one tick.
            // For example:
            // tick-0: Tile tick
            // tick-0: Part tick: update a value, and mark part to invalidate
            // tick-0: -- send all block updates to client ---
            // tick-1: Tile tick: notices and update, marks a block invalidate
            // tick-1: Part tick: update the value again, the old value has still not been sent here!
            // tick-1: -- send all block updates to client --- This will contain the value that was set in tick-1.
            state.onDirty();
            BlockHelpers.markForUpdate(target.getCenter().getPos().getLevel(true), target.getCenter().getPos().getBlockPos());
        }
    }

    @Override
    public boolean hasActiveVariable(IPartNetwork network, PartTarget target, S partState) {
        return partState.hasVariable();
    }

    @Override
    public <V extends IValue> IVariable<V> getActiveVariable(INetwork network, IPartNetwork partNetwork, PartTarget target, S partState) {
        return partState.getVariable(network, partNetwork);
    }

    protected void onValueChanged(INetwork network, IPartNetwork partNetwork, PartTarget target, S state,
                                  IValue lastValue, IValue newValue) {
        if (newValue == null) {
            state.setDisplayValue(null);
        } else {
            IValue materializedValue = null;
            try {
                if (newValue.getType() == ValueTypes.LIST) {
                    IValueTypeListProxy<IValueType<IValue>, IValue> original = ((ValueTypeList.ValueList) newValue).getRawValue();
                    if (original.getLength() > ValueTypeList.MAX_RENDER_LINES) {
                        List<IValue> list = Lists.newArrayList();
                        for (int i = 0; i < ValueTypeList.MAX_RENDER_LINES; i++) {
                            list.add(original.get(i));
                        }
                        newValue = ValueTypeList.ValueList.ofList(original.getValueType(), list);
                    }
                }
                materializedValue = newValue.getType().materialize(newValue);
            } catch (EvaluationException e) {
                state.addGlobalError(e.getErrorMessage());
            }
            state.setDisplayValue(materializedValue);
        }
    }

    @Override
    public Optional<MenuProvider> getContainerProvider(PartPos pos) {
        return Optional.of(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return new TranslatableComponent(getTranslationKey());
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
                Triple<IPartContainer, PartTypeBase, PartTarget> data = PartHelpers.getContainerPartConstructionData(pos);
                PartTypePanelVariableDriven.State partState = (PartTypePanelVariableDriven.State) data.getLeft().getPartState(data.getRight().getCenter().getSide());
                return new ContainerPartPanelVariableDriven(id, playerInventory, partState.getInventory(),
                        Optional.of(data.getRight()), Optional.of(data.getLeft()), (PartTypePanelVariableDriven<?, ?>) data.getMiddle());
            }
        });
    }

    @Override
    public void writeExtraGuiData(FriendlyByteBuf packetBuffer, PartPos pos, ServerPlayer player) {
        // Write inventory size
        IPartContainer partContainer = PartHelpers.getPartContainerChecked(pos);
        PartTypePanelVariableDriven.State partState = (PartTypePanelVariableDriven.State) partContainer.getPartState(pos.getSide());
        packetBuffer.writeInt(partState.getInventory().getContainerSize());

        super.writeExtraGuiData(packetBuffer, pos, player);
    }

    protected IgnoredBlockStatus.Status getStatus(PartTypePanelVariableDriven.State state) {
        IgnoredBlockStatus.Status status = IgnoredBlockStatus.Status.INACTIVE;
        if (state != null && !state.getInventory().isEmpty()) {
            if (state.hasVariable() && state.isEnabled()) {
                status = IgnoredBlockStatus.Status.ACTIVE;
            } else {
                status = IgnoredBlockStatus.Status.ERROR;
            }
        }
        return status;
    }

    @Override
    public BlockState getBlockState(IPartContainer partContainer,
                                    Direction side) {
        IgnoredBlockStatus.Status status = getStatus(partContainer != null
                ? (PartTypePanelVariableDriven.State) partContainer.getPartState(side) : null);
        return getBlock().defaultBlockState()
                .setValue(IgnoredBlock.FACING, side)
                .setValue(IgnoredBlockStatus.STATUS, status);
    }

    protected void onVariableContentsUpdated(IPartNetwork network, PartTarget target, S state) {
        state.onVariableContentsUpdated((P) this, target);
    }

    @Override
    public InteractionResult onPartActivated(final S partState, BlockPos pos, Level world, Player player, InteractionHand hand,
                                             ItemStack heldItem, BlockHitResult hit) {
        if(WrenchHelpers.isWrench(player, heldItem, world, pos, hit.getDirection())) {
            WrenchHelpers.wrench(player, heldItem, world, pos, hit.getDirection(),
                    (player1, pos1, parameter) -> partState.setFacingRotation(partState.getFacingRotation().getClockWise()));
            return InteractionResult.SUCCESS;
        }
        return super.onPartActivated(partState, pos, world, player, hand, heldItem, hit);
    }

    @Override
    public void loadTooltip(S state, List<Component> lines) {
        if (!state.getInventory().isEmpty()) {
            if (state.hasVariable() && state.isEnabled()) {
                IValue value = state.getDisplayValue();
                if(value != null) {
                    IValueType valueType = value.getType();
                    lines.add(new TranslatableComponent(
                            L10NValues.PART_TOOLTIP_DISPLAY_ACTIVEVALUE,
                            valueType.toCompactString(value).withStyle(valueType.getDisplayColorFormat()),
                            new TranslatableComponent(valueType.getTranslationKey())));
                }
            } else {
                lines.add(new TranslatableComponent(L10NValues.PART_TOOLTIP_ERRORS).withStyle(ChatFormatting.RED));
                for (MutableComponent error : state.getGlobalErrors()) {
                    lines.add(error.withStyle(ChatFormatting.RED));
                }
            }
        } else {
            lines.add(new TranslatableComponent(L10NValues.PART_TOOLTIP_INACTIVE));
        }
        super.loadTooltip(state, lines);
    }

    @Override
    public boolean shouldTriggerBlockRenderUpdate(@Nullable S oldPartState, @Nullable S newPartState) {
        return super.shouldTriggerBlockRenderUpdate(oldPartState, newPartState)
                || getStatus(oldPartState) != getStatus(newPartState);
    }

    public static abstract class State<P extends PartTypePanelVariableDriven<P, S>, S extends PartTypePanelVariableDriven.State<P, S>> extends PartStateActiveVariableBase<P> {

        @Getter
        @Setter
        private IValue displayValue;
        @Getter
        @Setter
        private Direction facingRotation = Direction.NORTH;

        public State() {
            super(1);
        }

        @Override
        public void writeToNBT(CompoundTag tag) {
            super.writeToNBT(tag);
            IValue value = getDisplayValue();
            if(value != null) {
                tag.putString("displayValueType", value.getType().getUniqueName().toString());
                tag.put("displayValue", ValueHelpers.serializeRaw(value));
            }
            tag.putInt("facingRotation", facingRotation.ordinal());
        }

        @Override
        public void readFromNBT(CompoundTag tag) {
            super.readFromNBT(tag);
            if(tag.contains("displayValueType", Tag.TAG_STRING)
                    && tag.contains("displayValue")) {
                IValueType valueType = ValueTypes.REGISTRY.getValueType(new ResourceLocation(tag.getString("displayValueType")));
                if(valueType != null) {
                    Tag serializedValue = tag.get("displayValue");
                    Component deserializationError = valueType.canDeserialize(serializedValue);
                    if(deserializationError == null) {
                        setDisplayValue(ValueHelpers.deserializeRaw(valueType, serializedValue));
                    } else {
                        IntegratedDynamics.clog(org.apache.logging.log4j.Level.ERROR, deserializationError.getString());
                    }
                } else {
                    IntegratedDynamics.clog(org.apache.logging.log4j.Level.ERROR,
                            String.format("Tried to deserialize the value \"%s\" for type \"%s\" which could not be found.",
                                    tag.getString("displayValueType"), tag.getString("value")));
                }
            } else {
                setDisplayValue(null);
            }
            facingRotation = Direction.values()[Math.max(2, tag.getInt("facingRotation"))];
        }
    }

}
