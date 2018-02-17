package org.cyclops.integrateddynamics.core.part.panel;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.network.event.INetworkEvent;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartTypeActiveVariable;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.client.gui.GuiPartDisplay;
import org.cyclops.integrateddynamics.core.block.IgnoredBlock;
import org.cyclops.integrateddynamics.core.block.IgnoredBlockStatus;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.WrenchHelpers;
import org.cyclops.integrateddynamics.core.network.event.NetworkElementAddEvent;
import org.cyclops.integrateddynamics.core.network.event.VariableContentsUpdatedEvent;
import org.cyclops.integrateddynamics.core.part.PartStateActiveVariableBase;
import org.cyclops.integrateddynamics.inventory.container.ContainerPartDisplay;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

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
        return new IgnoredBlockStatus(blockConfig);
    }

    @Override
    protected Map<Class<? extends INetworkEvent>, IEventAction> constructNetworkEventActions() {
        Map<Class<? extends INetworkEvent>, IEventAction> actions = super.constructNetworkEventActions();
        actions.put(VariableContentsUpdatedEvent.class, new IEventAction<P, S, VariableContentsUpdatedEvent>() {
            @Override
            public void onAction(INetwork network, PartTarget target, S state, VariableContentsUpdatedEvent event) {
                IPartNetwork partNetwork = NetworkHelpers.getPartNetwork(network);
                onVariableContentsUpdated(partNetwork, target, state);
            }
        });
        actions.put(NetworkElementAddEvent.Post.class, new IEventAction<P, S, NetworkElementAddEvent.Post>() {
            @Override
            public void onAction(INetwork network, PartTarget target, S state, NetworkElementAddEvent.Post event) {
                IPartNetwork partNetwork = NetworkHelpers.getPartNetwork(network);
                onVariableContentsUpdated(partNetwork, target, state);
            }
        });
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
                IVariable variable = state.getVariable(partNetwork);
                if(variable != null) {
                    newValue = variable.getValue();

                }
            } catch (EvaluationException e) {
                state.addGlobalError(new L10NHelpers.UnlocalizedString(e.getLocalizedMessage()));
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
            onVariableContentsUpdated(partNetwork, target, state);
            BlockHelpers.markForUpdate(target.getCenter().getPos().getWorld(), target.getCenter().getPos().getBlockPos());
        }
    }

    @Override
    public boolean hasActiveVariable(IPartNetwork network, PartTarget target, S partState) {
        return partState.hasVariable();
    }

    @Override
    public <V extends IValue> IVariable<V> getActiveVariable(IPartNetwork network, PartTarget target, S partState) {
        return partState.getVariable(network);
    }

    protected void onValueChanged(INetwork network, IPartNetwork partNetwork, PartTarget target, S state,
                                  IValue lastValue, IValue newValue) {
        if (newValue == null) {
            state.setDisplayValue(null);
        } else {
            IValue materializedValue = null;
            try {
                materializedValue = newValue.getType().materialize(newValue);
            } catch (EvaluationException e) {
                state.addGlobalError(new L10NHelpers.UnlocalizedString(e.getLocalizedMessage()));
            }
            state.setDisplayValue(materializedValue);
        }
    }

    @Override
    protected boolean hasGui() {
        return true;
    }

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerPartDisplay.class;
    }

    @Override
    public Class<? extends GuiScreen> getGui() {
        return GuiPartDisplay.class;
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
    public IBlockState getBlockState(IPartContainer partContainer,
                                     EnumFacing side) {
        IgnoredBlockStatus.Status status = getStatus(partContainer != null
                ? (PartTypePanelVariableDriven.State) partContainer.getPartState(side) : null);
        return getBlock().getDefaultState().withProperty(IgnoredBlock.FACING, side).
                withProperty(IgnoredBlockStatus.STATUS, status);
    }

    protected void onVariableContentsUpdated(IPartNetwork network, PartTarget target, S state) {
        state.onVariableContentsUpdated((P) this, target);
    }

    @Override
    public boolean onPartActivated(World world, BlockPos pos, final S partState, EntityPlayer player, EnumHand hand,
                                   ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if(WrenchHelpers.isWrench(player, heldItem, world, pos, side)) {
            WrenchHelpers.wrench(player, heldItem, world, pos, side, new WrenchHelpers.IWrenchAction<Void>() {
                @Override
                public void onWrench(EntityPlayer player, BlockPos pos, Void parameter) {
                    partState.setFacingRotation(partState.getFacingRotation().rotateAround(EnumFacing.Axis.Y));
                }
            });
            return true;
        }
        return super.onPartActivated(world, pos, partState, player, hand, heldItem, side, hitX, hitY, hitZ);
    }

    @Override
    public void loadTooltip(S state, List<String> lines) {
        if (!state.getInventory().isEmpty()) {
            if (state.hasVariable() && state.isEnabled()) {
                IValue value = state.getDisplayValue();
                if(value != null) {
                    IValueType valueType = value.getType();
                    lines.add(L10NHelpers.localize(
                            L10NValues.PART_TOOLTIP_DISPLAY_ACTIVEVALUE,
                            valueType.getDisplayColorFormat() + valueType.toCompactString(value),
                            L10NHelpers.localize(valueType.getUnlocalizedName())));
                }
            } else {
                lines.add(TextFormatting.RED + L10NHelpers.localize(L10NValues.PART_TOOLTIP_ERRORS));
                for (L10NHelpers.UnlocalizedString error : state.getGlobalErrors()) {
                    lines.add(TextFormatting.RED + error.localize());
                }
            }
        } else {
            lines.add(L10NHelpers.localize(L10NValues.PART_TOOLTIP_INACTIVE));
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
        private EnumFacing facingRotation = EnumFacing.NORTH;

        public State() {
            super(1);
        }

        @Override
        public void writeToNBT(NBTTagCompound tag) {
            super.writeToNBT(tag);
            IValue value = getDisplayValue();
            if(value != null) {
                tag.setString("displayValueType", value.getType().getUnlocalizedName());;
                tag.setString("displayValue", ValueHelpers.serializeRaw(value));
            }
            tag.setInteger("facingRotation", facingRotation.ordinal());
        }

        @Override
        public void readFromNBT(NBTTagCompound tag) {
            super.readFromNBT(tag);
            if(tag.hasKey("displayValueType", MinecraftHelpers.NBTTag_Types.NBTTagString.ordinal())
                    && tag.hasKey("displayValue", MinecraftHelpers.NBTTag_Types.NBTTagString.ordinal())) {
                IValueType valueType = ValueTypes.REGISTRY.getValueType(tag.getString("displayValueType"));
                if(valueType != null) {
                    String serializedValue = tag.getString("displayValue");
                    L10NHelpers.UnlocalizedString deserializationError = valueType.canDeserialize(serializedValue);
                    if(deserializationError == null) {
                        setDisplayValue(valueType.deserialize(serializedValue));
                    } else {
                        IntegratedDynamics.clog(Level.ERROR, deserializationError.localize());
                    }
                } else {
                    IntegratedDynamics.clog(Level.ERROR,
                            String.format("Tried to deserialize the value \"%s\" for type \"%s\" which could not be found.",
                                    tag.getString("displayValueType"), tag.getString("value")));
                }
            } else {
                setDisplayValue(null);
            }
            facingRotation = EnumFacing.values()[Math.max(2, tag.getInteger("facingRotation"))];
        }
    }

}
