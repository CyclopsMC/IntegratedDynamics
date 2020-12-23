package org.cyclops.integrateddynamics.core.part.panel;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.Level;
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
        for(int i = 0; i < state.getInventory().getSizeInventory(); i++) {
            ItemStack itemStack = state.getInventory().getStackInSlot(i);
            if(!itemStack.isEmpty()) {
                itemStacks.add(itemStack);
            }
        }
        state.getInventory().clear();
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
                state.addGlobalError(new TranslationTextComponent(e.getLocalizedMessage()));
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
            BlockHelpers.markForUpdate(target.getCenter().getPos().getWorld(true), target.getCenter().getPos().getBlockPos());
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
                state.addGlobalError(new TranslationTextComponent(e.getLocalizedMessage()));
            }
            state.setDisplayValue(materializedValue);
        }
    }

    @Override
    public Optional<INamedContainerProvider> getContainerProvider(PartPos pos) {
        return Optional.of(new INamedContainerProvider() {
            @Override
            public ITextComponent getDisplayName() {
                return new TranslationTextComponent(getTranslationKey());
            }

            @Nullable
            @Override
            public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                Triple<IPartContainer, PartTypeBase, PartTarget> data = PartHelpers.getContainerPartConstructionData(pos);
                PartTypePanelVariableDriven.State partState = (PartTypePanelVariableDriven.State) data.getLeft().getPartState(data.getRight().getCenter().getSide());
                return new ContainerPartPanelVariableDriven(id, playerInventory, partState.getInventory(),
                        Optional.of(data.getRight()), Optional.of(data.getLeft()), (PartTypePanelVariableDriven<?, ?>) data.getMiddle());
            }
        });
    }

    @Override
    public void writeExtraGuiData(PacketBuffer packetBuffer, PartPos pos, ServerPlayerEntity player) {
        // Write inventory size
        IPartContainer partContainer = PartHelpers.getPartContainerChecked(pos);
        PartTypePanelVariableDriven.State partState = (PartTypePanelVariableDriven.State) partContainer.getPartState(pos.getSide());
        packetBuffer.writeInt(partState.getInventory().getSizeInventory());

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
        return getBlock().getDefaultState()
                .with(IgnoredBlock.FACING, side)
                .with(IgnoredBlockStatus.STATUS, status);
    }

    protected void onVariableContentsUpdated(IPartNetwork network, PartTarget target, S state) {
        state.onVariableContentsUpdated((P) this, target);
    }

    @Override
    public ActionResultType onPartActivated(final S partState, BlockPos pos, World world, PlayerEntity player, Hand hand,
                                            ItemStack heldItem, BlockRayTraceResult hit) {
        if(WrenchHelpers.isWrench(player, heldItem, world, pos, hit.getFace())) {
            WrenchHelpers.wrench(player, heldItem, world, pos, hit.getFace(),
                    (player1, pos1, parameter) -> partState.setFacingRotation(partState.getFacingRotation().rotateY()));
            return ActionResultType.SUCCESS;
        }
        return super.onPartActivated(partState, pos, world, player, hand, heldItem, hit);
    }

    @Override
    public void loadTooltip(S state, List<ITextComponent> lines) {
        if (!state.getInventory().isEmpty()) {
            if (state.hasVariable() && state.isEnabled()) {
                IValue value = state.getDisplayValue();
                if(value != null) {
                    IValueType valueType = value.getType();
                    lines.add(new TranslationTextComponent(
                            L10NValues.PART_TOOLTIP_DISPLAY_ACTIVEVALUE,
                            valueType.toCompactString(value).mergeStyle(valueType.getDisplayColorFormat()),
                            new TranslationTextComponent(valueType.getTranslationKey())));
                }
            } else {
                lines.add(new TranslationTextComponent(L10NValues.PART_TOOLTIP_ERRORS).mergeStyle(TextFormatting.RED));
                for (IFormattableTextComponent error : state.getGlobalErrors()) {
                    lines.add(error.mergeStyle(TextFormatting.RED));
                }
            }
        } else {
            lines.add(new TranslationTextComponent(L10NValues.PART_TOOLTIP_INACTIVE));
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
        public void writeToNBT(CompoundNBT tag) {
            super.writeToNBT(tag);
            IValue value = getDisplayValue();
            if(value != null) {
                tag.putString("displayValueType", value.getType().getUniqueName().toString());
                tag.put("displayValue", ValueHelpers.serializeRaw(value));
            }
            tag.putInt("facingRotation", facingRotation.ordinal());
        }

        @Override
        public void readFromNBT(CompoundNBT tag) {
            super.readFromNBT(tag);
            if(tag.contains("displayValueType", Constants.NBT.TAG_STRING)
                    && tag.contains("displayValue")) {
                IValueType valueType = ValueTypes.REGISTRY.getValueType(new ResourceLocation(tag.getString("displayValueType")));
                if(valueType != null) {
                    INBT serializedValue = tag.get("displayValue");
                    ITextComponent deserializationError = valueType.canDeserialize(serializedValue);
                    if(deserializationError == null) {
                        setDisplayValue(ValueHelpers.deserializeRaw(valueType, serializedValue));
                    } else {
                        IntegratedDynamics.clog(Level.ERROR, deserializationError.getString());
                    }
                } else {
                    IntegratedDynamics.clog(Level.ERROR,
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
