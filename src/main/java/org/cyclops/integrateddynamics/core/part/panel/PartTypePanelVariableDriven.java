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
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.network.event.INetworkEvent;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.client.gui.GuiPartDisplay;
import org.cyclops.integrateddynamics.core.block.IgnoredBlock;
import org.cyclops.integrateddynamics.core.block.IgnoredBlockStatus;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.helper.WrenchHelpers;
import org.cyclops.integrateddynamics.core.network.event.VariableContentsUpdatedEvent;
import org.cyclops.integrateddynamics.core.part.PartStateActiveVariableBase;
import org.cyclops.integrateddynamics.inventory.container.ContainerPartDisplay;

import java.util.List;
import java.util.Map;

/**
 * A panel part that is driven by a contained variable.
 * @author rubensworks
 */
public abstract class PartTypePanelVariableDriven<P extends PartTypePanelVariableDriven<P, S>, S extends PartTypePanelVariableDriven.State<P, S>> extends PartTypePanel<P, S> {

    public PartTypePanelVariableDriven(String name) {
        super(name);
    }

    @Override
    protected Block createBlock(BlockConfig blockConfig) {
        return new IgnoredBlockStatus(blockConfig);
    }

    @Override
    protected Map<Class<? extends INetworkEvent<IPartNetwork>>, IEventAction> constructNetworkEventActions() {
        Map<Class<? extends INetworkEvent<IPartNetwork>>, IEventAction> actions = super.constructNetworkEventActions();
        actions.put(VariableContentsUpdatedEvent.class, new IEventAction<P, S, VariableContentsUpdatedEvent>() {
            @Override
            public void onAction(IPartNetwork network, PartTarget target, S state, VariableContentsUpdatedEvent event) {
                onVariableContentsUpdated(event.getNetwork(), target, state);
            }
        });
        return actions;
    }

    @Override
    public void addDrops(PartTarget target, S state, List<ItemStack> itemStacks, boolean dropMainElement) {
        for(int i = 0; i < state.getInventory().getSizeInventory(); i++) {
            ItemStack itemStack = state.getInventory().getStackInSlot(i);
            if(itemStack != null) {
                itemStacks.add(itemStack);
            }
        }
        state.getInventory().clear();
        state.onVariableContentsUpdated((P) this, target);
        super.addDrops(target, state, itemStacks, dropMainElement);
    }

    @Override
    public void beforeNetworkKill(IPartNetwork network, PartTarget target, S state) {
        super.beforeNetworkKill(network, target, state);
        state.onVariableContentsUpdated((P) this, target);
    }

    @Override
    public void afterNetworkAlive(IPartNetwork network, PartTarget target, S state) {
        super.afterNetworkAlive(network, target, state);
        state.onVariableContentsUpdated((P) this, target);
    }

    @Override
    public boolean isUpdate(S state) {
        return true;
    }

    @Override
    public void update(IPartNetwork network, PartTarget target, S state) {
        super.update(network, target, state);
        IValue lastValue = state.getDisplayValue();
        IValue newValue = null;
        if(state.hasVariable()) {
            try {
                IVariable variable = state.getVariable(network);
                if(variable != null) {
                    newValue = variable.getValue();

                }
            } catch (EvaluationException e) {
                state.addGlobalError(new L10NHelpers.UnlocalizedString(e.getLocalizedMessage()));
            }
        }
        if(!ValueHelpers.areValuesEqual(lastValue, newValue)) {
            onValueChanged(network, target, state, lastValue, newValue);
            state.sendUpdate();
        }
    }

    protected void onValueChanged(IPartNetwork network, PartTarget target, S state, IValue lastValue, IValue newValue) {
        state.setDisplayValue(newValue != null ? newValue.getType().materialize(newValue) : null);
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

    @Override
    public IBlockState getBlockState(IPartContainer partContainer,
                                     EnumFacing side) {
        IgnoredBlockStatus.Status status = IgnoredBlockStatus.Status.INACTIVE;
        if(partContainer != null) {
            PartTypePanelVariableDriven.State state = (PartTypePanelVariableDriven.State) partContainer.getPartState(side);
            if (state != null && !state.getInventory().isEmpty()) {
                if (state.hasVariable() && state.isEnabled()) {
                    status = IgnoredBlockStatus.Status.ACTIVE;
                } else {
                    status = IgnoredBlockStatus.Status.ERROR;
                }
            }
        }
        return getBlock().getDefaultState().withProperty(IgnoredBlock.FACING, side).
                withProperty(IgnoredBlockStatus.STATUS, status);
    }

    protected void onVariableContentsUpdated(IPartNetwork network, PartTarget target, S state) {
        state.onVariableContentsUpdated((P) this, target);
    }

    @Override
    public boolean onPartActivated(World world, BlockPos pos, final S partState, EntityPlayer player,
                                   EnumFacing side, float hitX, float hitY, float hitZ) {
        if(WrenchHelpers.isWrench(player, pos)) {
            WrenchHelpers.wrench(player, pos, new WrenchHelpers.IWrenchAction<Void>() {
                @Override
                public void onWrench(EntityPlayer player, BlockPos pos, Void parameter) {
                    partState.setFacingRotation(partState.getFacingRotation().rotateAround(EnumFacing.Axis.Y));
                }
            });
            return true;
        }
        return super.onPartActivated(world, pos, partState, player, side, hitX, hitY, hitZ);
    }

    @Override
    public void loadTooltip(S state, List<String> lines) {
        super.loadTooltip(state, lines);
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
                lines.add(EnumChatFormatting.RED + L10NHelpers.localize(L10NValues.PART_TOOLTIP_ERRORS));
                for (L10NHelpers.UnlocalizedString error : state.getGlobalErrors()) {
                    lines.add(EnumChatFormatting.RED + error.localize());
                }
            }
        } else {
            lines.add(L10NHelpers.localize(L10NValues.PART_TOOLTIP_INACTIVE));
        }
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
                tag.setString("displayValue", value.getType().serialize(value));
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
                    setDisplayValue(valueType.deserialize(tag.getString("displayValue")));
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
